# UITransfer
android ui skip utils
# 项目根build.gradle引入 
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
# app模块build.gradle引入 
    dependencies {
        compile 'com.github.lany192.UITransfer:annotation:1.0.0'
        annotationProcessor 'com.github.lany192.UITransfer:compiler:1.0.0'
    }

# 使用方法

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

###调用方法

普通跳转startActivity
    
    Transfer.from(MainActivity.this).toOneActivity().setAge(3).setName("哈哈哈").start();

返回结果跳转普通跳转startActivityForResult

    Transfer.from(MainActivity.this).toOneActivity().setAge(3).setName("哈哈哈").start(int requestCode);
