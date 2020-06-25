package com.gpxblog.exampleasan;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.system.ErrnoException;
import android.widget.TextView;

import static android.system.Os.setenv;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    // Example of a call to a native method
    TextView tv = (TextView) findViewById(R.id.sample_text);
    tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        prepareAsan();
        System.loadLibrary("native-lib");
    }


    private static void prepareAsan() {
        final String[] abis = Build.SUPPORTED_ABIS;
        for (String abi : abis) {
            String arch;
            if ("arm64-v8a".equals(abi)) {
                arch = "aarch64";
            } else if ("armeabi-v7a".equals(abi)) {
                arch = "arm";
            } else if ("armeabi".equals(abi)) {
                arch = "arm";
            } else if ("x86_64".equals(abi)) {
                arch = "x86_64";
            } else if ("x86".equals(abi)) {
                arch = "i686";
            } else {
                throw new RuntimeException("Unsupported ABI " + abi);
            }

            final String asan = "clang_rt.asan-" + arch + "-android";
            final String cpp = "c++_shared";

            try {
                System.loadLibrary(asan);
                try {
                    System.loadLibrary(cpp);
                    setenv("LD_PRELOAD", asan + " " + cpp, true);
                } catch(UnsatisfiedLinkError e) {
                    setenv("LD_PRELOAD", asan, true);
                }
                setenv("ASAN_OPTIONS","log_to_syslog=false,allow_user_segv_handler=1", true);
                return;
            } catch (UnsatisfiedLinkError | ErrnoException e) {
                e.printStackTrace();
            }
        }
    }


}
