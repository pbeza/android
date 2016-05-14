package pl.edu.pw.mini.intercom.GUI;

import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import pl.edu.pw.mini.intercom.R;

public class WiFiPeerListAdapter extends RecyclerView.Adapter<WiFiPeerListAdapter.ViewHolder> {

    private final List<WifiP2pDevice> devices;
    private final ViewHolder.OnItemClickListener listener;

    public WiFiPeerListAdapter(List<WifiP2pDevice> devices, ViewHolder.OnItemClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_row_devices, parent, false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final WifiP2pDevice device = devices.get(position);
        holder.deviceName.setText(device.deviceName);
        holder.deviceDetail.setText(device.toString());
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder /*implements ViewHolder.OnItemClickListener*/ {
        public final TextView deviceName;
        public final TextView deviceDetail;
        public final ViewHolder.OnItemClickListener listener;
        private WifiP2pDevice device;

        public ViewHolder(View view, ViewHolder.OnItemClickListener listener) {
            super(view);
            this.deviceName = (TextView) itemView.findViewById(R.id.device_name);
            this.deviceDetail = (TextView) itemView.findViewById(R.id.device_details);
            this.listener = listener;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + device.deviceAddress + deviceName.getText() + "'";
        }

//        @Override
//        public void onClick(WifiP2pDevice device, View view) {
//            listener.onClick(view);
//        }

        public void bind(final WifiP2pDevice device) {
            this.device = device;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(device, v);
                }
            });
        }

        public interface OnItemClickListener {
            void onClick(WifiP2pDevice device, View view);
        }
    }
}