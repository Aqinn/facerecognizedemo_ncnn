# facerecognizedemo_ncnn

![](https://img.shields.io/badge/license-MIT-blue) [![https://github.com/liguiyuan/mobilefacenet-ncnn](https://img.shields.io/badge/FaceDetect-RetainFace-yellow)](https://github.com/JavisPeng/ncnn-andriod-retainface) [![](https://img.shields.io/badge/FaceRecognize-MobileFaceNet-orange)](https://github.com/liguiyuan/mobilefacenet-ncnn)

Android 端的人脸识别 demo

Face recognition demo on Android
<br>
## 1. 简介（Introduction）
### 1.1 技术点（Technical Points）

人脸检测模型 - [![https://github.com/liguiyuan/mobilefacenet-ncnn](https://img.shields.io/badge/FaceDetect-RetainFace-yellow)](https://github.com/JavisPeng/ncnn-andriod-retainface)

人脸识别模型 - [![](https://img.shields.io/badge/FaceRecognize-MobileFaceNet-orange)](https://github.com/liguiyuan/mobilefacenet-ncnn)

移动端推理框架 - [NCNN](https://github.com/Tencent/ncnn)

## 2. 进度（Progress）
### 2.1 已完成（Completed）

1. 输入两张照片，分别检测出照片中的人脸，然后输入人脸识别模型得出人脸特征的余弦相似度
2. 接受视频流形式的输入，检测出人脸并在屏幕中绘制出人脸框（Face Box）和人脸关键点（Landmarks）
3. 接受视频流形式的输入，检测出人脸并在屏幕中绘制出人脸框（Face Box）和人脸关键点（Landmarks），并在屏幕下方输出当前人脸所属者名称，如无检测到人脸或识别出的人脸不包含在待检测特征向量中时，会有相应提示

### 2.2 待完成（TODO）

- [ ] 代码格式化/重构。本人正在全力完成毕设与准备春招，代码比较简陋，可能不适合上手阅读，现阶段主要作自己的测试demo 用，后面会重新写。

## 3. 参考与感谢（Reference and Thanks）

本项目代码主要参考以下 Github 编写，感谢 ~ <br>
https://github.com/liguiyuan/mobilefacenet-ncnn
<br>
https://github.com/JavisPeng/ncnn-andriod-retainface
<br>
代码编写过程中还有参考别的博客和 Github，有时间再一一例举 <br>

## License

MIT © Aqinn
