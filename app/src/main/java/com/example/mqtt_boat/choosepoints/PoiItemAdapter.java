package com.example.mqtt_boat.choosepoints;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.example.mqtt_boat.R;

import java.util.List;

public class PoiItemAdapter extends RecyclerView.Adapter {
    private List<PoiInfo> mPoiInfos; // poi信息

    private LayoutInflater mLayoutInflater;
    private Context mContext;

    private int mCurSelectPos = 0; // 当前选中的item pos

    private MyOnItemClickListener mOnItemClickListener;

    public PoiItemAdapter(List<PoiInfo> poiInfoList) {
        mPoiInfos = poiInfoList;
    }

    public void updateData(List<PoiInfo> poiInfos) {
        mPoiInfos = poiInfos;
        notifyDataSetChanged();
        mCurSelectPos = 0;
    }

    public void setOnItemClickListener(MyOnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.baidumap_rgc_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (position < 0) {
            return;
        }

        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (null == myViewHolder) {
            return;
        }

        modifyItemSelectState(myViewHolder, position);

        myViewHolder.mPoiItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == mOnItemClickListener) {
                    return;
                }


                int pos = myViewHolder.getAdapterPosition();
                if (pos == mCurSelectPos) {
                    return;
                }

                mCurSelectPos = pos;

                notifyDataSetChanged();

                if (null != mPoiInfos && pos < mPoiInfos.size()) {
                    PoiInfo poiInfo = mPoiInfos.get(pos);
                    mOnItemClickListener.onItemClick(pos, poiInfo);
                }
            }
        });

        bindViewHolder(myViewHolder, position);

    }

    /**
     * 由于Recyclerview的ViewHodler复用逻辑，导致滑动后，下一个绑定的item复用ViewHolder，会复用前一个item的选中状态
     * 这里需要对item的选中状态做下修正
     *
     * @param myViewHolder
     * @param position
     */
    private void modifyItemSelectState(MyViewHolder myViewHolder, int position) {
        if (position != mCurSelectPos) {
            myViewHolder.mImgCheck.setVisibility(View.GONE);
        } else if (position == mCurSelectPos) {
            myViewHolder.mImgCheck.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 绑定ViewHolder信息
     *
     * @param viewHolder
     * @param position
     */
    private void bindViewHolder(MyViewHolder viewHolder, int position) {
        if (null == mPoiInfos || position >= mPoiInfos.size()) {
            return;
        }

        if (0 == position) {
            PoiInfo poiInfo = mPoiInfos.get(0);
            bindAddressInfo(viewHolder, poiInfo);
        } else {
            bindPoiInfo(viewHolder, position);
        }
    }

    private void bindPoiInfo(MyViewHolder viewHolder, int position) {
        PoiInfo poiInfo = mPoiInfos.get(position);
        if (null == poiInfo) {
            return;
        }

        String name = poiInfo.getName();

        viewHolder.mPoiNameText.setText(name);
        viewHolder.mPoiAddressText.setText(poiInfo.getAddress());
        String poiAddress = poiInfo.getAddress();
        if (!TextUtils.isEmpty(poiAddress)) {
            if (View.INVISIBLE == viewHolder.mPoiAddressText.getVisibility()
                    || View.GONE == viewHolder.mPoiAddressText.getVisibility()) {
                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(viewHolder.mPoiNameText.getLayoutParams());
                layoutParams.setMargins(0, 40, 0, 0);
                viewHolder.mPoiNameText.setLayoutParams(layoutParams);
                viewHolder.mPoiAddressText.setVisibility(View.VISIBLE);
                viewHolder.mPoiAddressText.setText(poiAddress);
            }
            viewHolder.mPoiAddressText.setText(poiAddress);
        } else {
            hideAddressInfo(viewHolder);
        }
    }

    private void bindAddressInfo(MyViewHolder viewHolder, PoiInfo poiInfo) {
        String name = "【" + poiInfo.getAddress() + "】";
        viewHolder.mPoiNameText.setText(name);
        hideAddressInfo(viewHolder);
    }

    private void hideAddressInfo(MyViewHolder viewHolder) {
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(viewHolder.mPoiNameText.getLayoutParams());
        layoutParams.setMargins(0, 40, 0, 40);
        viewHolder.mPoiNameText.setLayoutParams(layoutParams);
        viewHolder.mPoiAddressText.setVisibility(View.VISIBLE);
    }

    /**
     * 获取item数目
     */
    @Override
    public int getItemCount() {
        if (null != mPoiInfos) {
            return mPoiInfos.size();
        }

        return 0;
    }

    /**
     * 定义item点击回调接口
     */
    public interface MyOnItemClickListener {
        void onItemClick(int position, PoiInfo poiInfo);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public View mPoiItemView;
        public TextView mPoiNameText;
        public TextView mPoiAddressText;
        public ImageView mImgCheck;

        public MyViewHolder(View view) {
            super(view);
            mPoiItemView = view;
            mPoiNameText = view.findViewById(R.id.poiResultName);
            mPoiAddressText = view.findViewById(R.id.poiResultDetail);
            mImgCheck = view.findViewById(R.id.imgCheck);
        }
    }
}
