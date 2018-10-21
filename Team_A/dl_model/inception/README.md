### 학습 실행

```
# python retrain.py \
    --bottleneck_dir=./workspace/bottlenecks \
    --model_dir=./workspace/inception \
    --output_graph=./workspace/cars_graph.pb \
    --output_labels=./workspace/cars_labels.txt \
    --image_dir ./workspace/car_datas \
    --how_many_training_steps 1000
```

### 추론 테스트

```
# python predict.py ./workspace/flower_photos/roses/20409866779_ac473f55e0_m.jpg
```

### retrain.py 주요 옵션

- --bottleneck_dir : 학습할 사진을 인셉션 용으로 변환해서 저장할 폴더
- --model_dir : inception 모델을 다운로드 할 경로
- --image_dir : 원본 이미지 경로
- --output_graph : 추론에 사용할 학습된 파일(.pb) 경로
- --output_labels : 추론에 사용할 레이블 파일 경로
- --how_many_training_steps : 얼만큼 반복 학습시킬 것인지

### 참고

- [텐서플로 모델 저장소](https://github.com/tensorflow/models)에서 더 많은 모델들을 다운로드 받고 시험해 볼 수 있습니다
- [텐서플로 저장소의 retrain.py 원본 위치](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/image_retraining)$

```
본코드는 3분 딥러닝 텐서플로맛에 나오는 inception코드를 변형한 코드입니다.
( model, graph, label 부분을 수정하였고, 데이터셋 크기가 다를 시 실행이 안되는 부분을 변형하였습니다 ) 
이미지는 http://vmmrdb.cecsresearch.org 에서 다운받으면 됩니다.

retrain.py에서 모델부분만 변형하면 잘 돌아갑니다
```
