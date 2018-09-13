package oxhammar.nicklas.run.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import oxhammar.nicklas.run.activities.DisplayFinishedRunActivity;
import oxhammar.nicklas.run.DBHandler;
import oxhammar.nicklas.run.FinishedRun;
import oxhammar.nicklas.run.R;

/**
 * Created by Nick on 2018-03-27.
 */

public class RunListAdapter extends RecyclerView.Adapter<RunListAdapter.ViewHolder> {

    private DBHandler db;

    private Context context;
    private LinearLayoutManager llm;
    private ArrayList<FinishedRun> runList;


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView vRunDuration;
        View cardView;
        ImageButton deleteRunImageButton;

        ViewHolder(View v) {
            super(v);

            vRunDuration = v.findViewById(R.id.run_card_duration_text);
            cardView = v.findViewById(R.id.card_view_run);
            deleteRunImageButton = v.findViewById(R.id.deleteRunImageButton);

        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RunListAdapter(Context context, LinearLayoutManager linearLayoutManager, ArrayList<FinishedRun> runList) {
        this.context = context;
        this.runList = runList;
        llm = linearLayoutManager;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public RunListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.run_card_view, parent, false);

        context = parent.getContext();
        db = new DBHandler(context);

        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RunListAdapter.ViewHolder holder, int position) {
        final FinishedRun finishedRun = runList.get(position);

        holder.vRunDuration.setText(finishedRun.getStringDistanceAndDate());
        holder.cardView.setTag(finishedRun.getStringDuration());
        holder.deleteRunImageButton.setTag(finishedRun.getDate().toString());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                displayFinishedRun(finishedRun);

            }
        });

        holder.deleteRunImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                buildAlertMessageDeleteRun(holder.getAdapterPosition());

            }
        });

    }


    private void removeAt(int position) {
        db.deleteRun(runList.get(position).getId());

        runList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, runList.size());

    }

    private void buildAlertMessageDeleteRun(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.delete_run))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        removeAt(position);
                    }
                })
                .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public int getItemCount() {
        return runList.size();
    }

    private void displayFinishedRun(final FinishedRun run) {
        Intent myIntent = new Intent(context, DisplayFinishedRunActivity.class);
        myIntent.putExtra("runId", run.getId());
        context.startActivity(myIntent);
    }
}
