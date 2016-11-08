package com.sumbioun.android.pitstop.contribute;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.customwidgets.StarGroup;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.map.JSONCommentDecoder;

import deprecated.RefuelActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

/*ContributeComment                                                            */
/*In this activity the user can post a comment about a particular gas station. */ 
public class ContributeComment extends RefuelActivity  {

	private float mRating;
	
	private CustTextView mAddress;
	
	private String mAddressLine = new String();
	private EditText mCommentBox;
	
	private Gasstation mGasstation;
	private String mComment;
	private SubmitComment submitComment;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contribute_comment);

        StarGroup averageRating = (StarGroup) this.findViewById(R.id.contribute_comment_average_rating);
        
        mAddress = (CustTextView) this.findViewById(R.id.contribute_comment_address);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	
            mRating = extras.getFloat(getString(R.string.EXTRA_CONTRIBUTECOMMENT_RATING));  	
        	mAddressLine = extras.getString(getString(R.string.EXTRA_CONTRIBUTECOMMENT_ADDRESS));
        	mGasstation = extras.getParcelable(getString(R.string.EXTRA_CONTRIBUTECOMMENT_GASSTATION));
        	
        	averageRating.setRating(mRating);
            mAddress.setText(mAddressLine);
            
        } else {
        	throw new RuntimeException();
        }
        
        mCommentBox = (EditText) this.findViewById(R.id.contribute_comment_comment_box);
        
        //Submits comment to external database if the text isn't null.
        TextView comment = (TextView) this.findViewById(R.id.contribute_comment_box);
        comment.setOnClickListener(new OnClickListener(){
        	
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ContributeComment.this, R.anim.clickresize));
				finishActivity();
			}
			
		});  
        
        //Skips activity.
       /* TextView skip = (TextView) this.findViewById(R.id.contribute_comment_skip);
        skip.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ContributeComment.this, R.anim.clickresize));
				ContributeComment.this.finish();
			}
			
		});*/
    }
    
    @Override
	protected void onStart() {
	    super.onStart(); 
	    MyApplication.startGps();
	    if(!MyApplication.getGps().canGetLocation()){
	    	MyApplication.getGps().showSettingsAlert();
	    }
	    
	}
	
	@Override
	public void onStop() {
	    super.onStop();  
	    
	    //Stops the location manager to spare the battery since the user is no longer using the application.
	    MyApplication.getGps().stopUsingGPS(); 
	    
	    if(submitComment != null){
	    	submitComment.cancel(true);
	    }

	}
    
    private void finishActivity(){
    	
    	/*if(mCommentBox.getText().toString().contentEquals("")){
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    	builder.setMessage("Adicione algum comentário antes de enviar.")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           
	            public void onClick(DialogInterface dialog, int id) {
            		dialog.dismiss();
	            }
	        });
	    	
	    	builder.create().show();
    	} else {*/
    		
    	if(mCommentBox.getText().toString().contentEquals("")){
    		
    		submitComment = new SubmitComment("");
    		submitComment.execute();
    		
    	} else {
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(ContributeComment.this);
    		
	    	builder.setTitle(getString(R.string.contribute_comment_username));
	    	final EditText input = new EditText(this);
	    	builder.setView(input);
	    	builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	           
	            public void onClick(DialogInterface dialog, int id) {

            		String username = input.getText().toString();
            		if(username.contentEquals("")){
            			username = "Anonymous";
            		}
            		submitComment = new SubmitComment(username);
            		submitComment.execute();
            		
            		dialog.dismiss();
	            }
	        });
	    	
	    	builder.create().show();
	    	
    	}
    		
    		
    	}

    //}
    
    //Submits the comment to the external database.
    class SubmitComment extends AsyncTask<Void, Void, Void>{
    	private ProgressDialog progressDialog;
    	private String mUsername = null;
    	
    	SubmitComment(String username){
    		this.mUsername = username;
    	}
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ContributeComment.this);
            progressDialog.setMessage(getString(R.string.contribute_comment_submiting_comment));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
        @Override
        protected Void doInBackground(Void... params) {

        	//Submits rating to the external database.
        	mComment = mCommentBox.getText().toString();

        	JSONCommentDecoder commentDecoder = new JSONCommentDecoder();
        	commentDecoder.sendCommentToServer(mGasstation.getId(), mUsername, mComment, mRating);

        	
			return null;
				
        }
        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param); 
            progressDialog.hide();  
            progressDialog.dismiss();
            ContributeComment.this.finish();
        	
        }
    }

}