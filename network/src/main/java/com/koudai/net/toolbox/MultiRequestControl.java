package com.koudai.net.toolbox;

import java.util.List;

/**
 * Created by zhaoyu on 15/12/8.
 */
public final class MultiRequestControl {

    List<RequestControl> controls;


    public MultiRequestControl(List<RequestControl> controls) {
        this.controls = controls;
    }

    public void cancelAll() {
        for (RequestControl control : controls) {
            if (control != null && !control.isFinished()) {
                control.cancel();
            }
        }
    }

    public void cancelAll(boolean isNotify) {
        for (RequestControl control : controls) {
            if (control != null && !control.isFinished()) {
                control.cancel(isNotify);
            }
        }
    }

    public void cancelOne(int which) {
        controls.get(which).cancel();
    }

    public void cancelOne(int which, boolean isNotify) {
        controls.get(which).cancel(isNotify);
    }
    
}
