# UITransfer
android ui skip utils
# root build.gradle 
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
# app build.gradle 
    dependencies {
        compile 'com.github.lany192.UITransfer:annotation:1.0.0'
        annotationProcessor 'com.github.lany192.UITransfer:compiler:1.0.0'
    }
# 使用
### 声明需要的参数
    public class OneActivity extends AppCompatActivity {
        @IntentExtra
        String name;
        @IntentExtra
        int age;
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Transfer.inject(this);
            ...
        }
    }
####调用方法
    Transfer.from(MainActivity.this).toOneActivity().setAge(3).setName("哈哈哈").start();
