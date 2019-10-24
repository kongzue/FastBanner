# Kongzue FastBanner
Kongzue FastBanner是一款快速创建轮播图的组件，仅需要简单配置即可满足绝大多数需要使用轮播图的场景。

<a href="https://github.com/kongzue/FastBanner/">
<img src="https://img.shields.io/badge/FastBanner-1.0.1-green.svg" alt="Kongzue Tabbar">
</a>
<a href="https://bintray.com/myzchh/maven/Banner/1.0.1/link">
<img src="https://img.shields.io/badge/Maven-1.0.1-blue.svg" alt="Maven">
</a>
<a href="http://www.apache.org/licenses/LICENSE-2.0">
<img src="https://img.shields.io/badge/License-Apache%202.0-red.svg" alt="License">
</a>
<a href="http://www.kongzue.com">
<img src="https://img.shields.io/badge/Homepage-Kongzue.com-brightgreen.svg" alt="Homepage">
</a>

Demo预览图如下：

![FastBanner](https://github.com/kongzue/FastBanner/raw/master/banner_preview.png)

Demo下载地址：

[点击下载](https://fir.im/fasebanner)

## 优势

- 快速实现，无需复杂配置，满足绝大多数轮播场景；

- 提供可定制化子界面的轮播，应对相对复杂的场景；

## 使用方法

1) 从 Maven 仓库或 jCenter 引入：
Maven仓库：
```
<dependency>
  <groupId>com.kongzue.banner</groupId>
  <artifactId>basebanner</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```
Gradle：
在dependencies{}中添加引用：
```
implementation 'com.kongzue.banner:basebanner:1.0.1'
```

2) 从XML布局文件创建：
```
<com.kongzue.basebanner.SimpleBanner xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/simpleBanner"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:indicatorNormal="@drawable/rect_white_alpha50"
    app:indicatorFocus="@drawable/rect_white_alpha90"
    app:indicatorGravity="center">
</com.kongzue.basebanner.SimpleBanner>
```

其中各属性解释如下：

字段 | 含义 | 默认值
---|---|---
indicatorNormal  | 指示器普通情况下的样式  | 半透明白色小圆点(R.drawable.rect_white_alpha50)
indicatorFocus  | 指示器焦点情况下的样式  | 白色小圆点(R.drawable.rect_white_alpha90)
indicatorGravity | 指示器所处位置  | 默认center(可选left, center, right)
indicatorMargin  | 指示器到边框的距离  | 15dp
delay  | 自动轮播延迟  | 4000毫秒
period  | 自动轮播周期  | 4000毫秒
autoPlay  | 自动播放  | true

3) 代码中设置要轮播的数据

FastBanner 并不自带图片显示框架，您可以自行选择 Fresco、Glide 或其他框架。

FastBanner 要通过代码来设置轮播数据，方法很简单，这里以 Fresco 举例：
```
//准备你的数据
List<String> imageUrls = new ArrayList<>();
imageUrls.add("http://example.com/test/fs/1.jpg");
imageUrls.add("http://example.com/test/fs/2.jpg");
imageUrls.add("http://example.com/test/fs/3.jpg");

//绑定数据
simpleBanner.setData(imageUrls, new SimpleBanner.BindData<SimpleDraweeView>(){
    @Override
    public void bind(String url, SimpleDraweeView imageView, int index) {
        imageView.setImageURI(url);
    }
});
```
代码中的接口 BindData 是用于绑定你的图片组件和内容的，它可以设置一个泛型，来确定你所使用的图片组件。

另外请注意检查你的网络访问权限，很多情况下无法显示轮播图内容的原因是因为未声明网络权限。

### 自定义布局的 CustomBanner

偶尔我们需要 Banner 的内容布局中添加一些动态文本，或者其他东西，此时可以使用 CustomBanner 来实现：

1) 从XML布局文件创建 CustomBanner：
```
<com.kongzue.basebanner.CustomBanner xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/customBanner"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:indicatorNormal="@drawable/rect_white_alpha50"
    app:indicatorFocus="@drawable/rect_white_alpha90"
    app:indicatorGravity="right">
</com.kongzue.basebanner.CustomBanner>
```

2) 准备一个内容布局 item_banner.xml 

可查看：[范例代码](https://github.com/kongzue/FastBanner/blob/master/app/src/main/res/layout/item_banner.xml)

3) 使用代码创建并绑定数据
```
//准备你的数据
List<Map<String,Object>> bannerData = new ArrayList<>();
Map<String,Object> data = new HashMap<>();
data.put("title","场馆标题A");
data.put("tip","北京市朝阳区某某东大街135号");
data.put("tip2","距离500M 1680人光顾");
data.put("img","http://example.com/test/fs/1.jpg");
bannerData.add(data);
data = new HashMap<>();
data.put("title","场馆标题B");
data.put("tip","北京市朝阳区某某东大街135号");
data.put("tip2","距离500M 1680人光顾");
data.put("img","http://example.com/test/fs/2.jpg");
bannerData.add(data);
data = new HashMap<>();
data.put("title","场馆标题C");
data.put("tip","北京市朝阳区某某东大街135号");
data.put("tip2","距离500M 1680人光顾");
data.put("img","http://example.com/test/fs/3.jpg");
bannerData.add(data);

//绑定数据
customBanner.setData(bannerData, R.layout.item_banner, new CustomBanner.BindView<Map<String,String>>() {
    @Override
    public void bind(Map<String, String> data, View rootView, int index) {
        SimpleDraweeView imgBkg = rootView.findViewById(R.id.img_bkg);
        TextView txtTitle = rootView.findViewById(R.id.txt_title);
        TextView txtAddress = rootView.findViewById(R.id.txt_address);
        TextView txtInfo = rootView.findViewById(R.id.txt_info);
        imgBkg.setImageURI(data.get("img")+"");
        txtTitle.setText(data.get("title")+"");
        txtAddress.setText(data.get("tip")+"");
        txtInfo.setText(data.get("tip2")+"");
    }
});
```
此时可以看到，CustomBanner 提供的接口 BindView 可以传入一个泛型，它决定了您的数据类型，可以如上述代码是一个 Map，也可以是您自定义的 JavaBean。

接口实现的方法中将子界面的数据（data）和子界面的根布局（rootView）返回，您可以使用 rootView.findViewById(resId) 来获取您的组件的实例化对象，并对其进行设值、绑定事件等操作。

## 开源协议
```
Copyright FastBanner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 更新日志
v1.0.2:
- 新增回调参数 index，该值为数据下标；

v1.0.1:
- 全新发布；


