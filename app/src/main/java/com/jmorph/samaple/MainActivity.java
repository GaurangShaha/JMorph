package com.jmorph.samaple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jmorph.morpher.EmployeeDAOMorpher;

import java.math.BigInteger;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EmployeeDAO employeeDAO = new EmployeeDAO();
        employeeDAO.setFirstName("Gaurang");
        employeeDAO.setLastName("Shaha");
        employeeDAO.setIdNumber(new BigInteger("127873871287382"));
        employeeDAO.setPhone("9130517000");
        employeeDAO.setEmail("gaurang.shaha@gmail.com");
        employeeDAO.setJobTitle("Android Developer");
        employeeDAO.setDepartmentId(1);
        employeeDAO.setSupervisor("John Doe");
        employeeDAO.setLocation("Blue ridge, Hinjewadi, Pune");
        employeeDAO.setEmployeeType(3);
        employeeDAO.setActive(true);
        employeeDAO.setStartDate(System.currentTimeMillis());
        Calendar endDateCalender = Calendar.getInstance();
        endDateCalender.add(Calendar.YEAR, 5);
        employeeDAO.setEndDate(endDateCalender.getTimeInMillis());
        employeeDAO.setDocumentLink("www.facebook.com/gaurang.shaha");


        ((TextView) findViewById(R.id.textViewOriginalObject)).append(employeeDAO.toString());
        ((TextView) findViewById(R.id.textViewMorphedObject)).append(EmployeeDAOMorpher.morph(employeeDAO).toString());

    }
}
