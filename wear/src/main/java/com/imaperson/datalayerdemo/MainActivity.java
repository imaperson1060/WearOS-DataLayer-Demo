package com.imaperson.datalayerdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {
	Button talkButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		talkButton = findViewById(R.id.talkButton);

		talkButton.setOnClickListener(view -> {
			String onClickMessage = "I just sent the handheld a message!";
			Toast.makeText(getApplicationContext(), onClickMessage, Toast.LENGTH_SHORT).show();

			new SendMessage(getString(R.string.path), onClickMessage).start();
		});

		IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
		Receiver messageReceiver = new Receiver();

		LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
	}

	public class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String onMessageReceived = "I just received a message from the handheld!";
			Toast.makeText(getApplicationContext(), onMessageReceived, Toast.LENGTH_SHORT).show();
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
			Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

			try {
				List<com.google.android.gms.wearable.Node> nodes = Tasks.await(nodeListTask);
				for (Node node : nodes) {
					Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

					try {
						Integer result = Tasks.await(sendMessageTask);
					} catch (ExecutionException exception) { }
					catch (InterruptedException exception) { }
				}
			} catch (ExecutionException exception) { }
			catch (InterruptedException exception) { }
		}
	}
}

// https://code.tutsplus.com/tutorials/get-wear-os-and-android-talking-exchanging-information-via-the-wearable-data-layer--cms-30986
