package com.make.develop.domi.Callback;

import com.make.develop.domi.Model.BestDealModel;


import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
