package com.imaperson.datalayerdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
	Button talkButton;
	protected Handler myHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		talkButton = findViewById(R.id.talkButton);

		talkButton.setOnClickListener(view -> {
			String onClickMessage = "I just sent the wearable a message!";
			Toast.makeText(getApplicationContext(), onClickMessage, Toast.LENGTH_SHORT).show();

			new SendMessage(getString(R.string.path), onClickMessage).start();
		});

		myHandler = new Handler(message -> {
			String messageText = message.getData().getString("messageText");
			if (messageText.compareTo("") != 0)
				Toast.makeText(getApplicationContext(), messageText, Toast.LENGTH_SHORT).show();
			return true;
		});

		IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
		Receiver messageReceiver = new Receiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
	}

	public class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String message = "I just received a message from the wearable!";
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		}
	}

	class SendMessage extends Thread {
		String path;
		String message;

		SendMessage(String path, String message) {
			this.path = path;
			this.message = message;
		}

		public void run() {
			Task<List<Node>> wearableList = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

			try {
				List<Node> nodes = Tasks.await(wearableList);
				for (Node node : nodes) {
					Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

					try {
						Tasks.await(sendMessageTask);
					} catch (ExecutionException | InterruptedException ignored) { }
				}
			} catch (ExecutionException | InterruptedException ignored) { }
		}
	}
}

// https://code.tutsplus.com/tutorials/get-wear-os-and-android-talking-exchanging-information-via-the-wearable-data-layer--cms-30986