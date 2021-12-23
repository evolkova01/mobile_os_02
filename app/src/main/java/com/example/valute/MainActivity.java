package com.example.valute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.service.controls.templates.ControlButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editableText, notEditableText;
    private Document doc; // принимаем веб страницу в текстовом виде
    private  Thread secThread; // второстепенный поток
    private  Runnable runnable; // отвечает, где будет запускать код
    private Spinner firstValute; // список
    private String[] valutesNames;
    private double[] valutesDosh;
    private int[] valutesMultiple;
    private ArrayAdapter<String> valutesAdapter; // для работы с элементами списка типа Spinner
    private int valuteId;

    public MainActivity() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editableText = findViewById(R.id.editableText);
        notEditableText = findViewById(R.id.notEditableText);
        notEditableText.setFocusable(false);
        notEditableText.setLongClickable(false);
        firstValute = findViewById(R.id.spinner);

        init();
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        // создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемента spinner
        // связывание содержимого из набора данных с элементами списка
        valutesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valutesNames);
        // настраиваем адаптер для связывания
        valutesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // вызываем метод setAdapter
        // применяем адаптер к элементу spinner
        firstValute.setAdapter(valutesAdapter);

        editableText.addTextChangedListener(new TextWatcher() {
            @Override
            // метод вызывается, чтобы уведомить вас о том, что в charSequence символы i1, начинающиеся с i, будут заменены новым текстом с длиной i2
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            // метод вызывается, чтобы уведомить вас о том, что символы i2, начинающиеся с i, только что заменили старый текст, который имел длину i1
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            // метод вызывается, чтобы уведомить нас, что где-то текст был изменен
            public void afterTextChanged(Editable editable) {
                if (editableText.getText().length() == 0) {
                    return;
                }
                // считаем курс
                double result = Double.parseDouble(editableText.getText().toString()) * valutesMultiple[valuteId] / valutesDosh[valuteId];
                notEditableText.setText("" + Math.round(result*100.0)/100.0);
            }
        });

                firstValute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    // можем обрабатывать выбор элемента из списка
                    // получаем объект события, объект-выбранный элемент, его индекс в адаптере, идентификатор строки
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (editableText.getText().length() == 0) return;
                        double result = Double.parseDouble(editableText.getText().toString()) * valutesMultiple[i] / valutesDosh[i];
                        notEditableText.setText("" + Math.round(result*100.0)/100.0);
                        valuteId = i;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
    }

    protected void getWeb(){
        try {
            // подключаемся к сайту
            doc = Jsoup.connect("https://cbr.ru/currency_base/daily/").get();
            Elements tables = doc.getElementsByTag("tbody"); // получаем элементы по ярлыку ("массив")
            Element our_table = tables.get(0); // наша таблица
            Elements all_valutes = our_table.children(); // массив элементов в таблице
            valutesNames = new String[all_valutes.size()-1];
            valutesDosh = new double[all_valutes.size()-1];
            valutesMultiple = new int[all_valutes.size()-1];

            for (int i = 1; i < all_valutes.size(); i++) {
                Element dollar = all_valutes.get(i); // получение элемента из таблицы
                Elements dollar_elements = dollar.children();
                String parsing = dollar_elements.get(4).text();
                parsing = parsing.replace(',', '.'); // заменить все вхождения
                valutesDosh[i-1] = Math.round(Double.parseDouble(parsing)*100.0)/100.0; // округление
                valutesNames[i-1] = dollar_elements.get(3).text(); // валюта
                valutesMultiple[i-1] = Integer.parseInt(dollar_elements.get(2).text().toString()); // сначала конвертируем в строку, потом в int
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private  void init() {
        runnable = new Runnable() {
            @Override
            public void run() {
                getWeb();
            } // запуск кода
        };
        secThread = new Thread(runnable);
        secThread.start(); // запуск потока
    }
}