package arteh.world.grpctest;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MainActivity extends AppCompatActivity {

    private EditText hostEdit;
    private EditText portEdit;
    private EditText messageEdit;
    private Button sendButton;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hostEdit = (EditText) findViewById(R.id.host);
        portEdit = (EditText) findViewById(R.id.port);
        messageEdit = (EditText) findViewById(R.id.message);
        sendButton = (Button) findViewById(R.id.send);
        resultText = (TextView) findViewById(R.id.result);
        resultText.setMovementMethod(new ScrollingMovementMethod());
    }

    private class GRPCConnect extends Thread {
        private ManagedChannel channel;
        String host, portStr, message, result;

        public GRPCConnect(String host, String portStr, String message) {
            this.host = host;
            this.portStr = portStr;
            this.message = message;
        }

        public void run() {
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.parseInt(portStr);
            try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
                HelloRequest request = HelloRequest.newBuilder().setName(message).build();
                HelloReply reply = stub.sayHello(request);
                result = reply.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            runOnUiThread(() -> {
                TextView resultText = (TextView) findViewById(R.id.result);
                Button sendButton = (Button) findViewById(R.id.send);
                resultText.setText(result);
                sendButton.setEnabled(true);
            });
        }
    }

    public void sendGrpcMessage(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
        sendButton.setEnabled(false);
        resultText.setText("");
        new GRPCConnect(hostEdit.getText().toString(),
                messageEdit.getText().toString(),
                portEdit.getText().toString()).start();
    }
}