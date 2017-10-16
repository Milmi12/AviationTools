package de.friction.aviationtools;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.timepicker.TimePickerBuilder;
import com.codetroopers.betterpickers.timepicker.TimePickerDialogFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class FDTCheckFragment extends Fragment implements View.OnClickListener, TimePickerDialogFragment.TimePickerDialogHandler {

    final int TIMEBUFFER = 20;
    int sectors = 1;
    int woclStart = 120; // WoCL begins at 0200h local
    int woclEnd = 360; // WoCL ends at 0600h local
    TextView checkInTextView, onBlockTextView;
    View btnCheckIn, btnOnBlock;
    CheckBox btnUTC;
    Button btnMinus, btnPlus;
    private int checkIn, onBlock, latestOnBlock, flightDutyTime, maxFlightDutyTime;
    private String checkInString, onBlockString, latestOnBlockString;
    private String timeZone = "LT"; // local time
    private boolean isCheckIn = true;
    private View rootView; // Declaration as global
    final int utcOffset = (java.util.TimeZone.getDefault().getRawOffset()
            + java.util.TimeZone.getDefault().getDSTSavings()) / 60000;
    // Time difference to UTC in minutes


    public FDTCheckFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_fdtcheck, container, false);
        btnCheckIn = rootView.findViewById(R.id.btn_check_in);
        btnCheckIn.setOnClickListener(this);

        btnOnBlock = rootView.findViewById(R.id.btn_on_block);
        btnOnBlock.setOnClickListener(this);

        btnUTC = (CheckBox) rootView.findViewById(R.id.checkbox_utc);
        btnUTC.setOnClickListener(this);

        btnMinus = (Button) rootView.findViewById(R.id.btn_minus);
        btnMinus.setOnClickListener(this);

        btnPlus = (Button) rootView.findViewById(R.id.btn_plus);
        btnPlus.setOnClickListener(this);

        checkInTextView = (TextView) rootView.findViewById(R.id.check_in);
        onBlockTextView = (TextView) rootView.findViewById(R.id.on_block);

        String homeZone = "GMT + " + utcOffset / 60;
        if (utcOffset < 0) {
            homeZone = "GMT - " + utcOffset / 60;
        }
        TextView textView = (TextView) rootView.findViewById(R.id.time_zone);
        textView.setText("" + homeZone);

    return rootView;

    } // End of onCreateView()


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check_in:
                isCheckIn = true;
                callTimePickerbuilder();
                break;

            case R.id.btn_on_block:
                isCheckIn = false;
                callTimePickerbuilder();
                break;

            case R.id.checkbox_utc:
                if (btnUTC.isChecked()) {
                    woclStart -= utcOffset;
                    woclEnd -= utcOffset;
                    timeZone = "Z";
                    Toast.makeText(getActivity(), "Timezone = UTC", Toast.LENGTH_SHORT).show();
                } else {
                    woclStart += utcOffset;
                    woclEnd += utcOffset;
                    timeZone = "LT";
                    Toast.makeText(getActivity(), "Timezone = local time", Toast.LENGTH_SHORT).show();
                }
                calculateTime();
                break;

            case R.id.btn_minus:
                decrement(rootView);
                break;
            case R.id.btn_plus:
                increment(rootView);
                break;
        }
    }


    private void callTimePickerbuilder() {
        TimePickerBuilder tpb = new TimePickerBuilder()
                .setFragmentManager(getActivity().getSupportFragmentManager())
                .setTargetFragment(this)
                .setStyleResId(R.style.BetterPickersDialogFragment);
        tpb.show();
    }

    /**
     * This method is called when the OK button inside the TimePickerFragment is pressed
     *
     * @param reference Return ist immer -1
     * @param hourOfDay enthält die eingegebene Stunde
     * @param minute    enthält die eingegeben Minuten
     */
    @Override
   public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        TextView textView = checkInTextView;

        //  Unterscheidung, ob CheckIn oder OnBlock eingegeben wurde
        if (isCheckIn) {
            // Zuweisung der Zeit als String
            checkInString = String.format("%02d", hourOfDay) + String.format("%02d", minute);
        } else {
            textView = onBlockTextView;
            // Zuweisung der Zeit als String
            onBlockString = String.format("%02d", hourOfDay) + String.format("%02d", minute);
        }

        // Eingegebene Zeit wird im entsprechendem Feld angezeigt.
        textView.setText("" + String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + " " + timeZone);
        calculateTime();
    }



    /**
     * This method is called when the - button is clicked
     *  to decrement the sectors by 1
     */
    private void decrement(View view) {
        if (sectors == 1) {
            // Show an error message as a toast
            Toast.makeText(getActivity(), "Not less than 1 sector", Toast.LENGTH_SHORT).show();
            return;  // Exit method early
        }
        sectors = sectors - 1;
        displaySectors(sectors);
        calculateTime();
    }


    /**
     * This method is called when the + button is clicked
     *  to increment the sectors by 1
     */
    private void increment(View view) {
        if (sectors == 6) {
            // Show an error message as a toast
            Toast.makeText(getActivity(), "Not more than 6 sectors", Toast.LENGTH_SHORT).show();
            return;  //Exit method early
        }
        sectors = sectors + 1;
        displaySectors(sectors);
        calculateTime();
    }


    /**
     * This method displays the given sectors value on the screen.
     */
    private void displaySectors(int number) {
        TextView sectorsTextView = (TextView) rootView.findViewById(R.id.number_sectors);
        sectorsTextView.setText("" + number);
    }


// _________________________________________________________________________________________________

    /**
     * Wird aufgerufen, um alle Berechnungen durchzuführen
     */
    public void calculateTime() {

        LinearLayout result = (LinearLayout) rootView.findViewById(R.id.ergebnis);

        /**
         *  Falls noch nichts eingegeben wurde, wird auch nicht berechnet
         */
        if (checkInString != null) {
            result.setVisibility(View.VISIBLE); // Berechnungen sichtbar schalten
            result.setBackgroundColor(Color.parseColor("#E0E0E0")); // BackgroundColor is normal (gray)

            checkIn = stringToInt(checkInString); // checkIn time in minutes

            // refresh checkIn output
            checkInTextView.setText("" + checkInString.substring(0, 2) + ":" + checkInString.substring(2, 4) + " " + timeZone);

            if (onBlockString == null) {
                // maximale DutyTime berechnen
                textOutput(((TextView) rootView.findViewById(R.id.max_fdt)), intToString(maxFDP()) + "h");
                //Berechnung der maximal erlaubten On Block Zeit
                latestOnBlock = zeitAddition(stringToInt(checkInString), maxFDP()); // latestOnBlock Time in Minuten
                latestOnBlockString = intToString(latestOnBlock);
                if (latestOnBlockString.length() == 4) {
                    latestOnBlockString = "0" + latestOnBlockString;
                }
                latestOnBlockString = latestOnBlockString + " " + timeZone;
                textOutput((TextView) rootView.findViewById(R.id.max_on_block), latestOnBlockString);
            }
        }
        if (onBlockString != null) {
            // onBlock auffrischen
            onBlockTextView.setText("" + onBlockString.substring(0, 2) + ":" + onBlockString.substring(2, 4) + " " + timeZone);
        }
        if (checkInString == null || onBlockString == null) {
            return; // Nothing to calculate or show
        }
        onBlock = stringToInt(onBlockString); // onBlock time in minutes



        // Berechnung der FlightDutyPeriod und Ausgabe
        flightDutyTime = timeDifference(checkInString, onBlockString);
        textOutput(((TextView) rootView.findViewById(R.id.flight_time_duty))
                , intToString(flightDutyTime) + "h");

        // Berechnung der gesetzlich maximal erlaubten FlightDutyPeriod
        maxFlightDutyTime = maxFDP();
        textOutput(((TextView) rootView.findViewById(R.id.max_fdt)), intToString(maxFlightDutyTime) + "h");

        //Berechnung der maximal erlaubten On Block Zeit
        latestOnBlock = zeitAddition(stringToInt(checkInString), maxFDP()); // latestOnBlock Time in Minuten
        latestOnBlockString = intToString(latestOnBlock);
        if (latestOnBlockString.length() == 4) {
            latestOnBlockString = "0" + latestOnBlockString;
        }
        latestOnBlockString = latestOnBlockString + " " + timeZone;
        textOutput((TextView) rootView.findViewById(R.id.max_on_block), latestOnBlockString);

        if (flightDutyTime + TIMEBUFFER >= maxFlightDutyTime) {
            // falls es knapp wird, Hintergrundfarbe GELB
            result.setBackgroundColor(Color.parseColor("#FFEB3B"));
        }
        if (flightDutyTime >= maxFlightDutyTime) {
            // falls illegal, Hintergrundfarbe ROT
            result.setBackgroundColor(Color.parseColor("#F44336"));
        }

    }


    /**
     * @param begin TimeToString Zeit
     * @param end   letzte onBlockTime Zeit
     * @return FlightDutyPeriod in Minuten
     */
    private int timeDifference(String begin, String end) {

        int fdp = stringToInt(end) - stringToInt(begin);

        // checken, ob über Mitternacht geflogen wird
        if (fdp < 0) { // falls Endzeit > Anfangszeit
            fdp += 24 * 60;
        }

        return fdp;
    }


    /**
     * Text an TextView übergeben
     *
     * @param textView Übergabe des relevanten Views
     * @param text     anzuzeigender Text
     */
    private void textOutput(TextView textView, String text) {
        textView.setText(text);
    }


    /**
     * Berechnung der maximalen FlightDutyTime
     *
     * @return int maxFlightDuty
     */
    private int maxFDP() {

        // Hole Eingabe Anzahl der Flüge
        TextView textView = (TextView) rootView.findViewById(R.id.number_sectors);
        int numberOfSectors = Integer.parseInt(textView.getText().toString());

        int maxFlightDuty = 13 * 60;

        // In der maxFlightDuty von 13h sind schon 2 Flüge enthalten
        // für jeden weiteren Flug werden 30 Minuten abgezogen
        while (numberOfSectors > 2) {
            maxFlightDuty -= 30;
            numberOfSectors -= 1;
        }
        return (maxFlightDuty - checkWocl(maxFlightDuty)); // Ist FDP innerhalb WocL?
    }


    /**
     * Integer in String wandeln
     *
     * @param time in Integer
     * @return String Zeit (HH:mm)
     */
    private String intToString(int time) {
        int hour = time / 60;
        int minute = time % 60;

        // falls minute einstellig ist, wird beim Return eine zusätzliche 0 vorangestellt
        String blank = "";
        if (minute < 10) {
            blank = "0";
        }
        return "" + hour + ":" + blank + minute;
    }


    /**
     * String in Integer wandeln
     *
     * @param time als String
     * @return time in Minuten als Integer
     */
    private int stringToInt(String time) {
        return Integer.parseInt(time.substring(0, 2)) * 60 + Integer.parseInt(time.substring(2, 4));
    }


    /**
     * 2 Zeiten werden addiert und überprüft, ob die Endzeit größer als 24:00h ist
     *
     * @param time1 checkIn in minutes
     * @param time2 onBlock in minutes
     * @return FlightDutyPeriod FDP
     */
    private int zeitAddition(int time1, int time2) {
        int addTime = time1 + time2;
        //falls Zeit >= 24:00h => 00:00h
        if (addTime >= 24 * 60) {
            addTime -= 24 * 60;
        }
        return addTime;
    }


    private int checkWocl(int maxFDP) {
        int anfang, ende;
        int latestOnBlock = zeitAddition(checkIn, maxFDP);

        Log.d("checkWocl", "WoclStart = " + woclStart);
        Log.d("checkWocl", "WoclEnd = " + woclEnd);
        Log.d("checkWocl", "checkIn = " + checkIn);
        Log.d("checkWocl", "latestOnBlock = " + latestOnBlock);

        // Prüfen, ob FDP über Mitternacht geht
        if (checkIn > latestOnBlock) {
            checkIn -= 24 * 60;
        }
        // Prüfen, ob Window of Circadian Low (0200h - 0600h) überhaupt in FDP liegt:
        if (checkIn >= woclEnd || latestOnBlock <= woclStart) {
            return 0;
        }

        // WocL komplett in FDP; Abzug von 2 Stunden
        if (checkIn <= woclStart && latestOnBlock >= woclEnd) {
            return 120;
        }

        // WocL partiell in FDP; woclStart = 120 L
        if (checkIn <= woclStart) {
            anfang = woclStart;
        } else {
            anfang = checkIn;
        } // woclEnd = 360 L
        if (latestOnBlock <= woclEnd) {
            ende = latestOnBlock;
        } else {
            ende = woclEnd;
        }
        return (ende - anfang) / 2;
    }
}
