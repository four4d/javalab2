package com.example.kim.life;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class ListAdapter extends BaseAdapter {
    private LayoutInflater _inflater;
    private static ArrayList<ListData> _lists;
    private int _layout;
    private static Context m_otx;
    private AsyncTask<Void, Void, Bitmap> mTask;

    public ListAdapter(Context context, int layout, ArrayList<ListData> lists) {
        _inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        _lists = lists;
        _layout = layout;
        m_otx = context;
    }

    @Override
    public int getCount() {
        return _lists.size();
    }

    @Override
    public String getItem(int position) {
        return _lists.get(position).getmDate();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = _inflater.inflate(_layout, parent, false);

            holder = new ViewHolder();
            holder.mPhoto = (ImageView) convertView.findViewById(R.id.mPhoto);
            holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
            holder.mText = (TextView) convertView.findViewById(R.id.mText);
            holder.voidbar3 = (TextView) convertView.findViewById(R.id.voidbar3);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.position = position;
        mTask = new ThumbnailTask(position, holder, m_otx);
        mTask.execute();

        return convertView;
    }
    private static class ThumbnailTask extends AsyncTask<Void, Void, Bitmap> {
        private int mPosition;
        private ViewHolder mHolder;
        private ListData list;
        int width;

        public ThumbnailTask(int position, ViewHolder holder, Context context) {
            mPosition = position;
            mHolder = holder;
            width = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            list = _lists.get(mPosition);

            mHolder.mPhoto.setVisibility(View.INVISIBLE);
            mHolder.mDate.setVisibility(View.INVISIBLE);
            mHolder.mText.setVisibility(View.INVISIBLE);
            mHolder.voidbar3.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Void... arg0) {
            Bitmap bm = null;
            String imagePath = list.getmPhoto();

            if (list.getmPhoto().equals("null")) {

            } else {
                try {
                    bm = BitmapFactory.decodeFile(imagePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.v("", "" + imagePath);
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            if (mHolder.position == mPosition) {
                mHolder.mPhoto.setVisibility(View.VISIBLE);
                mHolder.mDate.setVisibility(View.VISIBLE);
                mHolder.mText.setVisibility(View.VISIBLE);
                mHolder.voidbar3.setVisibility(View.VISIBLE);

                mHolder.mDate.setText(list.getmDate());
                mHolder.mText.setText(list.getmTitle());
                if (isCancelled()) {
                    bm = null;
                }
                if (bm != null) {
                    mHolder.mPhoto.setImageBitmap(bm);
                } else {
                    mHolder.mPhoto.setImageDrawable(null);
                }
            }
        }
    }
    private static class ViewHolder {
        public ImageView mPhoto;
        public TextView mText;
        public TextView mDate;
        public TextView voidbar3;
        public int position;
    }

}