package air.krusalovorg.tech;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.sax.RootElement;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText s_room;
    EditText height_room;
    RadioGroup group_light;
    RadioButton low_light;
    RadioButton normal_light;
    RadioButton hight_light;
    EditText people_num;
    EditText tech_num;
    CheckBox with_computer;
    CheckBox with_tv;
    EditText with_computer_num;
    EditText with_tv_num;
    TextView result_text;
    TextView bte_h_modal;
    TextView kvt_modal;
    LinearLayout air_contanier;
    Double kvt;
    Double bte;
    String error_code;

    final static double heat_from_people_q2 = 0.1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result_text = (TextView) findViewById(R.id.Result);

        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(5000);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);
        findViewById(R.id.snow_1).startAnimation(rotate);
        findViewById(R.id.snow_2).startAnimation(rotate);
        findViewById(R.id.snow_3).startAnimation(rotate);
        findViewById(R.id.snow_4).startAnimation(rotate);
    }

    public int getQFromLight(boolean low, boolean normal, boolean hight) {
        if (low) {
            return 30;
        } else if (normal) {
            return 35;
        } else if (hight) {
            return 40;
        }
        return 1;
    }

    public double getHeatRoom(double S, double H, int Q) {
        return S * H * Q / 1000;
    }

    public double getHeatFromTech(int tech_num, boolean include_computer, boolean include_tv, int computer_amount, int tv_amount) {
        double heat_tech = 0;
        int tech = 0;
        if (include_tv) {
            heat_tech += 0.2*tv_amount;
            tech += tv_amount;
        }
        if (include_computer) {
            heat_tech += 0.3*computer_amount;
            tech += computer_amount;
        }
        if (tech_num > tech) {
            heat_tech += (tech_num-tech)*0.2;
        }
        return heat_tech;
    }

    public double roundingKVT(double kvt) {
        int kvt_full = (int)kvt;
        double kvt_ost = kvt-kvt_full;
        if (kvt_ost < 0.5 && kvt_ost > 0.0) {
            return kvt+(0.5-kvt_ost);
        } else if (kvt_ost > 0.5 && kvt_ost < 1.0) {
            return kvt+(1.0-kvt_ost);
        }
        return kvt;
    }

    private void showBottomSheetDialog(double kvt, double bte) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet, null);
        LinearLayout layout_contanier_ = bottomSheetView.findViewById(R.id.bottom_sheet_contanier);

        LayoutInflater inflater = (LayoutInflater)getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RequestQueue queue = Volley.newRequestQueue(this);
        String proxy = "http://192.168.1.141:6990";
        String url = proxy+"/?bte="+bte;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRoot = new JSONObject(response);
                            JSONArray data = jsonRoot.getJSONArray("data");
                            if (data.length() > 0) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject item = data.getJSONObject(i);
                                    String name = item.getString("name");
                                    String link = item.getString("link");
                                    String global_image = item.getString("global_image");

                                    View air_ = inflater.inflate(R.layout.air, null);
                                    TextView title_ = (TextView) air_.findViewById(R.id.title);
                                    ImageView image_view_ = (ImageView) air_.findViewById(R.id.image_split);

                                    String image_url = proxy + "/get_image?image=" + global_image;
                                    System.out.println(image_url);

                                    Picasso.get().load(image_url).resize(100, 100).into(image_view_);

                                    title_.setText(name);
                                    air_.findViewById(R.id.bte).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent browserIntent = new
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                            startActivity(browserIntent);
                                        }
                                    });

                                    layout_contanier_.addView(air_);
                                }
                            } else {
                                TextView text_warn = new TextView(layout_contanier_.getContext());
                                text_warn.setText("Не найдено!");
                                text_warn.setTextColor(getColor(R.color.purple_500));
                                text_warn.setGravity(Gravity.CENTER_HORIZONTAL);
                                text_warn.setTextSize(20);
                                layout_contanier_.addView(text_warn);
                            }
                        } catch (JSONException e) {
                            System.out.println("error:" +e);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                View air_ = inflater.inflate(R.layout.air, null);
                TextView title_ = (TextView) air_.findViewById(R.id.title);
                title_.setText("Ошибка!");
                System.out.println(error);
            }
        });
        queue.add(stringRequest);

        //layout_contanier_.addView(air_);

        bottomSheetDialog.setContentView(layout_contanier_);
        bottomSheetDialog.show();

        bte_h_modal = (TextView) bottomSheetDialog.findViewById(R.id.bte_h);
        kvt_modal = (TextView) bottomSheetDialog.findViewById(R.id.kvt);
        air_contanier = (LinearLayout) bottomSheetDialog.findViewById(R.id.air_contanier);

        String bte_str = "БТЕ/Ч: "+Double.toString(bte);
        System.out.println(bte_str);
        String kvt_str = "КВТ: "+Double.toString(kvt);

        bte_h_modal.setText(bte_str);
        kvt_modal.setText(kvt_str);
    }

    public String getTextById(int id) {
        return getText(id).toString();
    }

    public String getMsgByErrorCode(String s) {
        switch (s) {
            case "m_q":
                return getTextById(R.string.error_m_q);
            case "m":
                return getTextById(R.string.error_m);
            case "light":
                return getTextById(R.string.error_light);
            case "people_num":
                return getTextById(R.string.error_people_num);
            case "tech_num":
                return getTextById(R.string.error_tech_num);
            default:
                return getTextById(R.string.error);
        }
    }

    public void openGithubOnClick() {
        Intent browserIntent = new
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link)));
        startActivity(browserIntent);
    }

    public void aboutApp(View v) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_info, null);
        bottomSheetView.findViewById(R.id.app_creator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGithubOnClick();
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void processing(View v) {
        String result = CalcPower();
        System.out.println("RESULT: "+result);
        System.out.println("ERROR CODE: "+error_code);
        switch (result) {
            case "success":
                showBottomSheetDialog(kvt, bte);
                break;
            case "not_fill_form":
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(getTextById(R.string.error_modal_title))
                        .setMessage(getMsgByErrorCode(error_code))
                        .setCancelable(true)
                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() { // Кнопка ОК
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //указываем функцию
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                //указываем функцию
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

                break;
            default:
                break;
        }
    }

    public boolean thisNull(String value) {
        return value.equals("");
    }
    public boolean radioButtonIsChecked(boolean button_1, boolean button_2, boolean button_3) {
        if (button_1 || button_2 || button_3) {
            return true;
        }
        return false;
    }

    public void setErrorCode(String code) {
        error_code = code;
        return;
    }

    public String ReturnFormNotFill(String error_code) {
        setErrorCode(error_code);
        return "not_fill_form";
    }

    public String CalcPower() {
        //S room
        s_room = (EditText)findViewById(R.id.s_room);
        if (thisNull(s_room.getText().toString())) {return ReturnFormNotFill("m_q");}
        double s_room_value_d = Double.parseDouble(s_room.getText().toString());
        float s_room_value_f = Float.parseFloat(s_room.getText().toString());
        //Height room
        height_room = (EditText)findViewById(R.id.height_room);
        if (thisNull(height_room.getText().toString())) {return ReturnFormNotFill("m");}
        double height_room_value_d = Double.parseDouble(height_room.getText().toString());
        float height_room_value_f = Float.parseFloat(height_room.getText().toString());
        //Light level
        low_light = (RadioButton) findViewById(R.id.low_light);
        normal_light = (RadioButton) findViewById(R.id.normal_light);
        hight_light = (RadioButton) findViewById(R.id.hight_light);

        boolean low_light_value = low_light.isChecked();
        boolean normal_light_value = normal_light.isChecked();
        boolean hight_light_value = hight_light.isChecked();

        if (!radioButtonIsChecked(low_light_value, normal_light_value, hight_light_value)) {return ReturnFormNotFill("light");}
        //People num
        people_num = (EditText) findViewById(R.id.people_num);
        if (thisNull(people_num.getText().toString())) {return ReturnFormNotFill("people_num");}
        int people_num_value = Integer.parseInt(people_num.getText().toString());

        //Tech num
        tech_num = (EditText) findViewById(R.id.tech_num);
        if (thisNull(tech_num.getText().toString())) {return ReturnFormNotFill("tech_num");}
        int tech_num_value = Integer.parseInt(tech_num.getText().toString());

        //Tech
        with_computer = (CheckBox) findViewById(R.id.with_computer);
        with_tv = (CheckBox) findViewById(R.id.with_tv);

        boolean with_computer_value = with_computer.isChecked();
        boolean with_tv_value = with_tv.isChecked();

        // Tech amount
        with_computer_num = (EditText) findViewById(R.id.with_computer_num);
        with_tv_num = (EditText) findViewById(R.id.with_tv_num);

        int with_computer_num_value = Integer.parseInt(with_computer_num.getText().toString().equals("") ? "1" : with_computer_num.getText().toString());
        int with_tv_num_value =  Integer.parseInt(with_tv_num.getText().toString().equals("") ? "1" : with_tv_num.getText().toString());

        // Q1 Heat room
        int q_room = getQFromLight(low_light_value, normal_light_value, hight_light_value);
        double heat_room_q1 = getHeatRoom(s_room_value_d > 0 ? s_room_value_d : s_room_value_f, height_room_value_d > 0? height_room_value_d : height_room_value_f, q_room);
        System.out.println("Heat room: "+heat_room_q1);

        // Q2 Heat Tech
        double heat_tech_q3 = getHeatFromTech(tech_num_value, with_computer_value, with_tv_value, with_computer_num_value, with_tv_num_value);
        System.out.println("Heat Tech: "+heat_tech_q3);

        // Result
        double result_ = heat_room_q1 + heat_from_people_q2 + heat_tech_q3;
        System.out.println("Result: "+result_);
        double result_min_ = result_-(5 * result_ / 100);
        double result_max_ = result_+(15 * result_ / 100);

        double result_round = roundingKVT(result_);
        double bte_in_hour = result_ / 0.000293;
        double bte_in_hour_round = roundingKVT(bte_in_hour);

        kvt = result_round;
        bte = bte_in_hour_round;

        //result_text.setText("Оптимальная мощность кондиционера: " + result_min_ + " < " + result_round + " < " + result_max_ + " кВт ( "+bte_in_hour_round+" БТЕ/Ч)");

        return "success";
    }
}