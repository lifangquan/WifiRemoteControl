package cn.myself.wifiremotecontrol;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by li on 15/11/4.
 */
public class MouseFragment extends Fragment {

    MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        LinearLayout touchView = new LinearLayout(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        touchView.setLayoutParams(params);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            private PointF p = new PointF();
            private PointF pd = new PointF();
            private int count = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        p.set(motionEvent.getX(), motionEvent.getY());
                        pd.set(p);
                        count++;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        count++;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        PointF p_move = new PointF(motionEvent.getX(), motionEvent.getY());
                        if (motionEvent.getPointerCount() == 2) {
                            Log.d("123", "ACTION_DRAG : " + (p_move.x - p.x) + "," + (p_move.y - p.y));
                            activity.sendAction("ACTION_DRAG : " + (p_move.x - p.x) + "," + (p_move.y - p.y));
                        } else if (motionEvent.getPointerCount() == 1) {
                            Log.d("123", "ACTION_MOVE : " + (p_move.x - p.x) + "," + (p_move.y - p.y));
                            activity.sendAction("ACTION_MOVE : " + (p_move.x - p.x) + "," + (p_move.y - p.y));
                        }
                        p = p_move;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("123", "ACTION_UP");
                        if (getDistance(pd, p) < 20) {
                            if (count == 2)
                                activity.sendAction("ACTION_RIGHT_CLICK");
                            else
                                activity.sendAction("ACTION_LEFT_CLICK");
                        }
                        count = 0;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        return touchView;
    }

    private float getDistance(PointF p1, PointF p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
}
