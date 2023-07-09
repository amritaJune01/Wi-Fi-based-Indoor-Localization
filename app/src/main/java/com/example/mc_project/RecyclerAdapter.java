package com.example.mc_project;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<ListContents> listContents;
    private Context context;

    public RecyclerAdapter(List<ListContents> listContents, Context context) {
        this.listContents = listContents;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, viewGroup, false);
        return new ViewHolder(v);
    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container = popupWindow.getContentView().getRootView();
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.4f;
        wm.updateViewLayout(container, p);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder viewHolder, int i) {
        ListContents item = listContents.get(i);
        viewHolder.txt.setText(item.getTxt());
        viewHolder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, item.getTxt(), Toast.LENGTH_SHORT).show();
//                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View pv = LayoutInflater.from(context).inflate(R.layout.popup_contact, null);
                PopupWindow popupWindow = new PopupWindow(pv);
//                , WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setWidth(750);
                popupWindow.setHeight(1000);
                popupWindow.showAtLocation(pv, Gravity.CENTER, 0, 0);
                dimBehind(popupWindow);
//                popupWindow.setBackgroundDrawable();
//                Drawable dim = new ColorDrawable(Color.BLACK);
//                dim.setBounds(0, 0, view.getWidth(), view.getHeight());
//                dim.setAlpha((int) (255 * .50));

//                ViewGroupOverlay overlay = (ViewGroupOverlay) view.getOverlay();
//                overlay.add(dim);

//                viewHolder.rv.getBackground().setAlpha(128);
                ImageButton close;
                close = (ImageButton) pv.findViewById(R.id.closeBtn);
                TextView name, status, loc;
                String n, s, l;
                n = item.getName();
                l = item.getLoc();
                status = (TextView) pv.findViewById(R.id.statusText);
                long curr_timestamp = (System.currentTimeMillis());
                if (Math.abs(curr_timestamp - item.getTimestamp()) <= 250000) {
                    s = "Online";
                    status.setText(s);
                    status.setTextColor(0xFF669900);

                } else {
                    s = "Offline";
                    status.setText(s);
                    status.setTextColor(Color.DKGRAY);
                }
                name = (TextView) pv.findViewById(R.id.nameText);

                loc = (TextView) pv.findViewById(R.id.locText);
                name.setText(n);
//                status.setText(s);
                loc.setText(l);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
//                        viewHolder.rv.getBackground().setAlpha(0);
//                        ViewGroupOverlay overlay = view.getOverlay();
//                        overlay.clear();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return listContents.size();
    }

    public void updateArrayList(ArrayList<ListContents> listContents) {
        this.listContents = listContents;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txt;
        public LinearLayout ll;
        public RecyclerView rv;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
//            ListFragment.nouser.setVisibility(View.GONE);
            txt = (TextView) itemView.findViewById((R.id.cardText));
            ll = (LinearLayout) itemView.findViewById(R.id.cardLL);
            rv = (RecyclerView) itemView.findViewById(R.id.RecyclerView);
        }
    }
}