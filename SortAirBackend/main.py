import flask
from flask import Flask, request, jsonify
import os
import json

app = Flask(__name__)
percent = 10

def getAirByBte(bte: float, percent: float, airs: list) -> list:
    bte_percent = bte * percent / 100
    bte_min = bte - bte_percent
    bte_max = bte + bte_percent

    result = []

    for air in airs:
        air_bte = float(air.get("bte"))
        if air_bte >= bte_min and air_bte <= bte_max:
            result.append(air)

    return result

@app.route('/get_image')
def get_image():
    args = request.args
    image_link = args.get("image")
    if image_link == None:
        return flask.send_file("./images/split1.jpg")

    return flask.send_file("."+image_link)

@app.route('/')
def get_recomend():
    args = request.args
    bte = args.get("bte")
    if bte != None:
        bte = float(bte)
    else:
        return jsonify(data=[])

    result = getAirByBte(bte, percent, airs)

    return jsonify(data=result)

if __name__ == '__main__':
    if not os.path.exists("./airs.json"):
        airs = open("airs.json", "w")
        airs.write(
            json.dumps(
                [{
                    "name": "Samsung AR9500T",
                    "bte": 5000,
                    "kvt": 10,
                    "simple_bte": 5,
                    "local_image": "./images/split1.jpg",
                    "global_image": "/images/split1.jpg",
                    "link": "https://www.samsung.com/ru/air-conditioners/wall-mount/ar09tshzawkner/"
                },
                {
                    "name": "Сплит-система #2",
                    "bte": 4800,
                    "kvt": 8,
                    "simple_bte": 5,
                    "local_image": "./images/split2.webp",
                    "global_image": "/images/split2.webp",
                    "link": "https://www.samsung.com/ru/air-conditioners/all-air-conditioners/"
                }
                ]
            )
        )
    airs = open("airs.json", "r")
    airs = json.loads(airs.read())
    app.run(host="0.0.0.0", port=6990)