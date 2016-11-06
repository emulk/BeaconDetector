package beacondetector.emulk.it.beacondetector;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by asd on 15/07/16.
 */
public class ShowBeaconAdapter extends BaseAdapter {
    private static final String TAG = "BeaconAdapter";
    private static ArrayList<BeaconStructure> beaconArrayList = new ArrayList<>();

    private LayoutInflater mInflater = null;

    public ShowBeaconAdapter(Context context, ArrayList<BeaconStructure> results) {
        beaconArrayList = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return beaconArrayList.size();
    }

    public Object getItem(int position) {
        return beaconArrayList.get(position);
    }


    @Override
    public boolean isEnabled(int position) {
        return false;
    }


    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        try {


            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.beacon_structure_row, null);
                holder = new ViewHolder();
                holder.AppName = (TextView) convertView.findViewById(R.id.AppName);
                holder.nameBeacon = (TextView) convertView.findViewById(R.id.nameBeacon);
                holder.namespaceID = (TextView) convertView.findViewById(R.id.namespaceID);
                holder.namespaceLayout = (TextView) convertView.findViewById(R.id.namespaceLayout);
                holder.instanceID = (TextView) convertView.findViewById(R.id.instanceID);
                holder.instanceLayout = (TextView) convertView.findViewById(R.id.instanceLayout);
                holder.beaconDistance = (TextView) convertView.findViewById(R.id.beaconDistance);
                holder.rssiView = (TextView) convertView.findViewById(R.id.rssiView);
                holder.TxPower = (TextView) convertView.findViewById(R.id.TxPower);
                holder.aliveId = (TextView) convertView.findViewById(R.id.aliveId);
                holder.urlLayout = (TextView) convertView.findViewById(R.id.urlLayout);
                holder.urlId = (TextView) convertView.findViewById(R.id.urlId);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }



            holder.AppName.setText(beaconArrayList.get(position).getBeaconProtocol());
            holder.nameBeacon.setText(beaconArrayList.get(position).getBeaconName());
            holder.namespaceID.setText(beaconArrayList.get(position).getBeaconID());
            holder.namespaceLayout.setText(beaconArrayList.get(position).getNamespaceLayout());
            holder.instanceID.setText(beaconArrayList.get(position).getInstance());
            holder.instanceLayout.setText(beaconArrayList.get(position).getInstanceLayout());
            holder.beaconDistance.setText(beaconArrayList.get(position).getDistance());
            holder.rssiView.setText(beaconArrayList.get(position).getRssi());
            holder.TxPower.setText(beaconArrayList.get(position).getTxPower());
            holder.aliveId.setText(beaconArrayList.get(position).getLastAliveOn());
            holder.urlId.setText(beaconArrayList.get(position).getUrlId());

            //se non ho un eddyston url nascondo i campi
            if (beaconArrayList.get(position).getUrlId().equalsIgnoreCase("")) {

                holder.urlId.setVisibility(View.INVISIBLE);
                holder.urlLayout.setVisibility(View.INVISIBLE);
            }


        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            Log.d(TAG, "Beacon Adapter");
        }
        return convertView;
    }

    static class ViewHolder {
        TextView AppName;
        TextView nameBeacon;
        TextView beaconLayout;
        TextView namespaceID;
        TextView namespaceLayout;
        TextView instanceID;
        TextView instanceLayout;
        TextView beaconDistance;
        TextView rssiView;
        TextView TxPower;
        TextView aliveId;
        TextView urlLayout;
        TextView urlId;


    }
}
