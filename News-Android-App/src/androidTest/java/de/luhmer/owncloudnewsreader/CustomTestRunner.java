package de.luhmer.owncloudnewsreader;

import android.app.Application;
import android.content.Context;

import androidx.test.runner.AndroidJUnitRunner;

public class CustomTestRunner extends AndroidJUnitRunner {

    /*
    @Override
    public void onCreate(Bundle arguments) {
        // The workaround for Mockito issue #922
        // https://github.com/mockito/mockito/issues/922

        arguments.putString("notPackage", "net.bytebuddy");
        super.onCreate(arguments);
    }
    */

    public Application newApplication(ClassLoader cl, String className, Context context) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        return super.newApplication(cl, TestApplication.class.getName(), context);
    }
}



