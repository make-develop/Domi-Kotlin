package com.make.develop.domi.ui.fooddetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.make.develop.domi.Common.Common;
import com.make.develop.domi.Database.CartDataSource;
import com.make.develop.domi.Database.CartDatabase;
import com.make.develop.domi.Database.CartItem;
import com.make.develop.domi.Database.LocalCartDataSource;
import com.make.develop.domi.EventBus.CounterCartEvent;
import com.make.develop.domi.Model.AddonModel;
import com.make.develop.domi.Model.CommentModel;
import com.make.develop.domi.Model.FoodModel;
import com.make.develop.domi.Model.SizeModel;
import com.make.develop.domi.R;
import com.make.develop.domi.ui.comments.CommentFragment;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment implements TextWatcher {

      private FoodDetailViewModel foodDetailViewModel;

      private CartDataSource cartDataSource;
      private CompositeDisposable compositeDisposable = new CompositeDisposable();

      private Unbinder unbinder;

      private android.app.AlertDialog waitingDialog;
      private BottomSheetDialog addonBottomSheetDialog;

      //View need inflate

    ChipGroup chip_group_addon;
    EditText edt_search;

      @BindView(R.id.img_food)
      ImageView img_food;
      @BindView(R.id.btnCart)
      CounterFab btnCart;
      @BindView(R.id.btn_rating)
      FloatingActionButton btn_rating;
      @BindView(R.id.food_name)
      TextView food_name;
      @BindView(R.id.food_description)
      TextView food_description;
      @BindView(R.id.food_price)
      TextView food_price;
      @BindView(R.id.number_button)
      ElegantNumberButton numberButton;
      @BindView(R.id.ratingBar)
      RatingBar ratingBar;
      @BindView(R.id.btnShowComment)
      Button btnShowComment;
      @BindView(R.id.rdi_group_size)
      RadioGroup rdi_group_size;

      @BindView(R.id.img_add_addon)
      ImageView img_add_addon;
      @BindView(R.id.chip_group_user_selected)
        ChipGroup chip_group_user_selected_addon;

      @OnClick(R.id.img_add_addon)
      void onAddonClick()
      {
            if (Common.selectedFood.getAddon() != null)
            {
                displayAddonList(); //Show all addon options
                addonBottomSheetDialog.show();
            }
      }

      @OnClick(R.id.btnCart)
      void onCartItemAdd()
      {
          CartItem cartItem = new CartItem();
          cartItem.setUid(Common.currentUser.getUid());
          cartItem.setUserPhone(Common.currentUser.getPhone());

          cartItem.setFoodId(Common.selectedFood.getId());
          cartItem.setFoodName(Common.selectedFood.getName());
          cartItem.setFoodImage(Common.selectedFood.getImage());
          cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
          cartItem.setFoodQuantity(Integer.valueOf(numberButton.getNumber()));
          cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(), Common.selectedFood.getUserSelectedAddon())); //Because default we not choose size + addon so extra price is 0

          if (Common.selectedFood.getUserSelectedAddon() != null)
              cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
          else
              cartItem.setFoodAddon("Default");
          if (Common.selectedFood.getUserSelectedSize() != null)
              cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
          else
              cartItem.setFoodSize("Default");

          cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                  cartItem.getFoodId(),
                  cartItem.getFoodSize(),
                  cartItem.getFoodAddon())
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new SingleObserver<CartItem>() {
                      @Override
                      public void onSubscribe(Disposable d) {

                      }

                      @Override
                      public void onSuccess(CartItem cartItemFromDB) {
                          if (cartItemFromDB.equals(cartItem))
                          {
                              //Already in the database, just update
                              cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                              cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                              cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                              cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity() + cartItem.getFoodQuantity());

                              cartDataSource.updateCartItems(cartItemFromDB)
                                      .subscribeOn(Schedulers.io())
                                      .observeOn(AndroidSchedulers.mainThread())
                                      .subscribe(new SingleObserver<Integer>() {
                                          @Override
                                          public void onSubscribe(Disposable d) {

                                          }

                                          @Override
                                          public void onSuccess(Integer integer) {
                                              Toast.makeText(getContext(), "Update Cart success", Toast.LENGTH_SHORT).show();
                                              EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                          }

                                          @Override
                                          public void onError(Throwable e) {
                                              Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                          }
                                      });

                          }
                          else
                          {
                              //Item not available in cart before, insert new
                              compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                      .subscribeOn(Schedulers.io())
                                      .observeOn(AndroidSchedulers.mainThread())
                                      .subscribe(() -> {
                                          Toast.makeText(getContext(), "Add to Cart success", Toast.LENGTH_SHORT).show();
                                          EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                      }, throwable -> {
                                          Toast.makeText(getContext(), "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                      }));

                          }
                      }

                      @Override
                      public void onError(Throwable e) {
                          if (e.getMessage().contains("empty"))
                          {
                              //Default, if cart is empty , this code will be fired

                              compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                      .subscribeOn(Schedulers.io())
                                      .observeOn(AndroidSchedulers.mainThread())
                                      .subscribe(() -> {
                                          Toast.makeText(getContext(), "Add to Cart success", Toast.LENGTH_SHORT).show();
                                          EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                      }, throwable -> {
                                          Toast.makeText(getContext(), "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                      }));

                          }
                          else
                              Toast.makeText(getContext(), "[GET CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                      }
                  });
      }

    private void displayAddonList() {
          if (Common.selectedFood.getAddon().size() > 0)
          {
              chip_group_addon.clearCheck(); //clear check all views
              chip_group_addon.removeAllViews();

              edt_search.addTextChangedListener(this);

              //Add all View

              for (AddonModel addonModel:Common.selectedFood.getAddon())
              {

                      Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                      chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                              .append(addonModel.getPrice()).append(")"));
                      chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                          @Override
                          public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                              if (b)
                              {
                                  if (Common.selectedFood.getUserSelectedAddon() == null)
                                      Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                                  Common.selectedFood.getUserSelectedAddon().add(addonModel);
                              }
                          }
                      });
                      chip_group_addon.addView(chip);

                  }
          }
    }

    @OnClick(R.id.btn_rating)
      void onRatingButtonClick()
      {
        showDialogRating();
      }

      @OnClick(R.id.btnShowComment)
      void onShowCommentButtonClick(){
          CommentFragment commentFragment = CommentFragment.getInstance();
          commentFragment.show(getActivity().getSupportFragmentManager(), "CommentFragment");
      }

    private void showDialogRating() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Rating Food");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating, null );

        final RatingBar ratingBar = (RatingBar)itemView.findViewById(R.id.rating_bar);
        final EditText edt_comment = (EditText)itemView.findViewById(R.id.edt_comment);

        builder.setView(itemView);

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                CommentModel commentModel = new CommentModel();
                commentModel.setName(Common.currentUser.getName());
                commentModel.setUid(Common.currentUser.getUid());
                commentModel.setComment(edt_comment.getText().toString());
                commentModel.setRatingValue(ratingBar.getRating());
                Map<String, Object> serverTimeStamp = new HashMap<>();
                serverTimeStamp.put("timeStamp", ServerValue.TIMESTAMP);
                commentModel.setCommentTimeStamp(serverTimeStamp);

                foodDetailViewModel.setCommentModel(commentModel);

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                                      ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel =
                ViewModelProviders.of(this).get(FoodDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);
        unbinder = ButterKnife.bind(this, root);
        initViews();
        foodDetailViewModel.getMutableLiveDataFood().observe(this, new Observer<FoodModel>() {
            @Override
            public void onChanged(FoodModel foodModel) {
                displayInfo(foodModel);
            }
        });
        foodDetailViewModel.getMutableLiveDataComment().observe(this, new Observer<CommentModel>() {
            @Override
            public void onChanged(CommentModel commentModel) {
                submitRatingToFirebase(commentModel);
            }
        });
        return root;
    }

    private void initViews() {

          cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

          waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

          addonBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.DialogStyle);
          View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
          chip_group_addon = (ChipGroup) layout_addon_display.findViewById(R.id.chip_group_addon);
          edt_search = (EditText)layout_addon_display.findViewById(R.id.edt_search);
          addonBottomSheetDialog.setContentView(layout_addon_display);

          addonBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
              @Override
              public void onDismiss(DialogInterface dialogInterface) {
                  displayUserSelectedAddon();
                  calculateTotalPrice();
              }
          });
      }

    private void displayUserSelectedAddon() {
          if (Common.selectedFood.getUserSelectedAddon() != null &&
          Common.selectedFood.getUserSelectedAddon().size() > 0)
          {
              chip_group_user_selected_addon.removeAllViews(); //Clear all view already added
              for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon()) //Add all available addon to list
              {
                  Chip chip =(Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                  chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                  .append(addonModel.getPrice()).append(")"));
                  chip.setClickable(false);
                  chip.setOnCloseIconClickListener(view -> {
                      //Remove whe user select delete
                      chip_group_user_selected_addon.removeView(view);
                      Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                      calculateTotalPrice();
                  } );
                  chip_group_user_selected_addon.addView(chip);

              }

          }else if(Common.selectedFood.getUserSelectedAddon().size() == 0)
              chip_group_user_selected_addon.removeAllViews();
    }

    private void submitRatingToFirebase(final CommentModel commentModel) {
            waitingDialog.show();
          //Firts, we will submit to Comments Ref
        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            //After submit to CommentRef, we will update value aveger in Food
                            addRatingToFood(commentModel.getRatingValue());
                        }
                        waitingDialog.dismiss();

                    }
                });
    }

    private void addRatingToFood(final float ratingValue) {
          FirebaseDatabase.getInstance()
                  .getReference(Common.CATEGORY_REF)
                  .child(Common.categorySelected.getMenu_id()) //Select Category
        .child("foods") //Select array list 'foods' of this category
        .child(Common.selectedFood.getKey()) //Because food item is array list so key is index of arraylist
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    final FoodModel foodModel = dataSnapshot.getValue(FoodModel.class);
                    foodModel.setKey(Common.selectedFood.getKey()); //Don't forget set it

                    //Apply Rating
                    if (foodModel.getRatingValue() == null)
                         foodModel.setRatingValue(0d); // d = D lower case
                    if (foodModel.getRatingCount() == null)
                         foodModel.setRatingCount(0l); // l = L lower case, not 1 (number one)
                    double sumRating = foodModel.getRatingValue()+ratingValue;
                    long ratingCount = foodModel.getRatingCount()+1;
                    double result = sumRating / ratingCount;

                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("ratingValue", result);
                    updateData.put("ratingCount", ratingCount);

                    //Update  data in variable
                    foodModel.setRatingValue(result);
                    foodModel.setRatingCount(ratingCount);

                    dataSnapshot.getRef()
                            .updateChildren(updateData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    waitingDialog.dismiss();
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(getContext(), "Thank you !", Toast.LENGTH_SHORT).show();
                                        Common.selectedFood = foodModel;
                                        foodDetailViewModel.setFoodModel(foodModel); //call refresh
                                    }
                                }
                            });
                }
                else
                    waitingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                waitingDialog.dismiss();
                Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));

        if (foodModel.getRatingValue() != null)
              ratingBar.setRating(foodModel.getRatingValue().floatValue());

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.selectedFood.getName());

        //Size
        for (SizeModel sizeModel: Common.selectedFood.getSize())
        {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b)
                        Common.selectedFood.setUserSelectedSize(sizeModel);
                    calculateTotalPrice(); //Update price
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT
            , 1.0f);

            radioButton.setLayoutParams(params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());

            rdi_group_size.addView(radioButton);
        }

        if (rdi_group_size.getChildCount() > 0)
        {
            RadioButton radioButton = (RadioButton)rdi_group_size.getChildAt(0);
            radioButton.setChecked(true); // Default first select
        }
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
          double totalPrice = Double.parseDouble(Common.selectedFood.getPrice().toString()), displayPrice= 0.0;

          //Addon
        if (Common.selectedFood.getUserSelectedAddon() != null && Common.selectedFood.getUserSelectedAddon().size() > 0)
            for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon())
                totalPrice+=Double.parseDouble(addonModel.getPrice().toString());


          //Size

        totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());
        displayPrice = totalPrice * (Integer.parseInt(numberButton.getNumber()));
        displayPrice = Math.round(displayPrice*100.0/100.0);

        food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //NOTHING
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();

        for (AddonModel addonModel:Common.selectedFood.getAddon())
        {
            if (addonModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase()))
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b)
                        {
                            if (Common.selectedFood.getUserSelectedAddon() == null)
                                Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                            Common.selectedFood.getUserSelectedAddon().add(addonModel);
                        }
                    }
                });
                chip_group_addon.addView(chip);

            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onStop() {
          compositeDisposable.clear();
        super.onStop();
    }
}


