package com.example.abhishekjpr.newmovieproject;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements Communicator{

    boolean mTwoPane = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.fragment_dettaaill)!=null){
            mTwoPane =true;
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.aa, new DetailFragment(), "My Detail Fragment");
        }
        else{
             mTwoPane = false;
        }
    }


    @Override
    public void sendMovieId(String id) {

        FragmentManager manager = getFragmentManager();
        DetailFragment fragment = (DetailFragment) manager.findFragmentById(R.id.fragment_dettaaill);
        fragment.receiveMovieId(id);
    }
}
