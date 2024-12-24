from typing import Union
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pandas as pd
import warnings
import sys
import emergency1218_2138 as em

warnings.filterwarnings("ignore", category=DeprecationWarning)

path = './'
naver_id, naver_key = 'ztoukfrsyz', 'by6OZFl0dfROReNDIRiBSCmIItYbu5uYfb8saL8I'

app = FastAPI()

class Log(BaseModel):
    datetime: str
    input_text: str
    input_summary: str
    input_latitude: float
    input_longitude: float
    em_class: int
    hospital1: str
    addr1: str
    tel1: str
    eta1: str
    dist1: float
    fee1: int
    path1: str
    hospital2: str
    addr2: str
    tel2: str
    eta2: str
    dist2: float
    fee2: int
    path2: str
    hospital3: str
    addr3: str
    tel3: str
    eta3: str
    dist3: float
    fee3: int
    path3: str

class Emergency(BaseModel):
    status: str
    lat: float
    lon: float

def process_hospital_recommendation(hospital_recommender):
    try:
        hospital_recommender.load_api_key()
        hospital_recommender.text_summary()
        transcript, result_r, ilat, ilon, result_t = hospital_recommender.classify_situation()
        result = hospital_recommender.search_map()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

    if isinstance(result, str):
        return {"result": result}

    if not result.empty:
        row_data = result.iloc[0].to_dict()
        return Log(**row_data)
    else:
        raise HTTPException(status_code=404, detail="No hospital found.")

@app.get("/")
def read_root():
    return {"Hello": "World"}

@app.put("/items/{filename}")
def update_item(filename: str):
    hospital_recommender = em.RecommendHospital3(
        naver_id=naver_id,
        naver_key=naver_key,
        path=path,
        filename=filename
    )

    log_instance = process_hospital_recommendation(hospital_recommender)

    return log_instance.dict()

@app.get("/items/{item_id}")
def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}

@app.post("/items/text")
def update_item_text(emer: Emergency):
    hospital_recommender = em.RecommendHospital3(
        naver_id=naver_id,
        naver_key=naver_key,
        path=path,
        transcript=emer.status,
        latitude=emer.lat,
        longitude=emer.lon
    )

    log_instance = process_hospital_recommendation(hospital_recommender)

    return log_instance.dict()
