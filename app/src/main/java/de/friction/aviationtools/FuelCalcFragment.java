package de.friction.aviationtools;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FuelCalcFragment extends Fragment {


    public FuelCalcFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fuel_calc, container, false);
        Button calcButton = (Button) rootView.findViewById(R.id.calculate_button);
        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateFuel(getView());
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    /** wird aufgerufen, sobald der Button gedrückt wird */

    public void calculateFuel(View view) {

        EditText editText;
        Integer remainingFuel = 0;
        Integer rampFuel = 0;
        Integer uplift = 0;
        int calcLiter = 0;
        String eingabe = "";
        double output = 0.0;

        editText = (EditText) getView().findViewById(R.id.remaining_fuel);
        eingabe = editText.getText().toString();
        remainingFuel = Integer.valueOf(eingabe);
        output = ( (double) remainingFuel / 10);
        editText.setText("" + output + "oo");
        remainingFuel = remainingFuel * 100;

        editText = (EditText) getView().findViewById(R.id.ramp_fuel);
        eingabe = editText.getText().toString();
        rampFuel = Integer.valueOf(eingabe);
        output = ( (double) rampFuel / 10);
        editText.setText("" + output + "oo");
        rampFuel = Integer.valueOf(eingabe) * 100;

        uplift = rampFuel - remainingFuel;
        calcLiter = (int) ((uplift * 0.567) + 0.5);
        TextView textUplift = (TextView) getView().findViewById(R.id.uplift);
        output = ( (double) uplift / 1000);
        textUplift.setText("" + output + "oo");

        output = ( (double) calcLiter / 1000);
        TextView textCalcLiters = (TextView) getView().findViewById(R.id.calc_liters);
        textCalcLiters.setText("" + output);
    }

}
