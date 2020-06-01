package com.make.develop.domi.ui.fooddetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.make.develop.domi.Common.Common;
import com.make.develop.domi.Model.CommentModel;
import com.make.develop.domi.Model.FoodModel;
import com.make.develop.domi.R;

import org.w3c.dom.Comment;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodDetailFragment extends Fragment {

      private FoodDetailViewModel foodDetailViewModel;

      private Unbinder unbinder;

      private android.app.AlertDialog waitingDialog;

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

      @OnClick(R.id.btn_rating)
      void onRatingButtonClick()
      {
        showDialogRating();
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
          waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
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
    }
}


