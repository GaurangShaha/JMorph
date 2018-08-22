# JMorph

## Version

## Use case
While implementing [Clean Architecture](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html), we ended up creating different POJO classes for each layers.

Lets consider a scenario where you have two objects named as EmployeeDTO and EmployeeDAO. EmployeeDTO belongs to domain layer whereas EmployeeDAO belongs to data layer.

```java
public class EmployeeDTO {
    private String lastName;
    private String firstName;
    private BigInteger idNumber;
    
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public BigInteger getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(BigInteger idNumber) {
        this.idNumber = idNumber;
    }
}
```

```java
public class EmployeeDAO {
    private String lastName;
    private String firstName;
    private BigInteger idNumber;
    
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public BigInteger getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(BigInteger idNumber) {
        this.idNumber = idNumber;
    }
}
```

EmployeeDAO is the object receiver from employee repository. In order to execute some functionality in interactor we need to pass EmployeeDTO.

To copy attribute value from EmployeeDAO to EmployeeDTO and vice versa, we used to write a code something similar to what shown below.

```java
public final class EmployeeDAOMorpher {
  public static EmployeeDTO morph(EmployeeDAO source) {
    EmployeeDTO target = new EmployeeDTO();
    target.setLastName(source.getLastName());
    target.setFirstName(source.getFirstName());
    target.setIdNumber(source.getIdNumber());
    return target;
  }

  public static EmployeeDAO reverseMorph(EmployeeDTO target) {
    EmployeeDAO source = new EmployeeDAO();
    source.setLastName(target.getLastName());
    source.setFirstName(target.getFirstName());
    source.setIdNumber(target.getIdNumber());
    return source;
  }
}
```

Let the JMorph library handle this tedious and error-prone task for you. You can auto generate above class using annotation provided by JMorph library.

## How to use

Add the library dependency to your build.gradle file.

```groovy
dependencies {
    implementation 'com.jmorph:jmorph-annotation:1.0.0'
    annotationProcessor 'com.jmorph:jmorph-compiler:1.0.1'
}
```

If you are using any other libraries with AnnotationsProcessors like ButterKnife, Realm, Dagger , etc. You need to set this in your build.gradle to exclude the Processor that is already packaged:

```groovy
packagingOptions {
    exclude 'META-INF/services/javax.annotation.processing.Processor'
}
```

Annotate source class and rebuild the project. JMorph library will generate a morpher class, which will have name as _&lt;yourClassName&gt;_Morpher.java

In this you will find two methods named as morph and reverseMorph. These can be used to copy attributes of object. 

#### @MorphTo(TargetClass.class)

This annotation is used on class which needs be morphed. It expects class of target in which it should be morphed.

In order to generate the morpher, source and target class JMorph library need following thing

* Both the classes should be of type public class i.e. interface, annotation, enum, private and protected class, abstract class can't be used with it.
* Attributes which you need to copy should have getter and setter for them.
* Either default or public no-arg constructor should be present in both the classes.

JMorph library will auto compare the attributes from both the classes and creates the morpher for you. Morpher will include the attributes which has same name and compatible data types.

If attribute has different names, you can instruct JMorph library to generate mapping using [@MorphToField](#morphtofieldfieldname) annotation. 

If attribute has different data types, you can instruct JMorph library to generate mapping using [@FieldTransformer](#fieldtransformerfieldtransformerclass) annotation.

Note : No annotation will be needed on target class.

#### @MorphToField("fieldName")

This annotation is used on attribute which has different name in both class. It expects attribute name from target class with whom it need be morphed.

It can be used with conjunction with [@FieldTransformer](#fieldtransformerfieldtransformerclass) annotation.

Note : This annotation should be used within class which is annotated with [@MorphTo](#morphtotargetclassclass). JMorph library will ignore this annotation if used outside of class annotated by [@MorphTo](#morphtotargetclassclass).

If attributes annotated with this has incompatible data type, JMorph library will ignore it.

#### @FieldTransformer(FieldTransformer.class)

This annotation is used on attribute which has different data type in both class. It expects a class which helps library to transform the fields.

Transformer class needs to implement FieldTransformerContract interface provided by JMorph library. Provide implementation for transform and reverseTransform method, library will take care of calling appropriate method.

```java
public class MillisToDateStringTransformer implements FieldTransformerContract<Long, String> {

    private final String DATE_FORMAT = "dd MMM yyyy";

    @Override
    public String transform(Long millis) {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date(millis));
    }

    @Override
    public Long reverseTransform(String date) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date).getTime();
        } catch (ParseException e) {
            return (long) -1;
        }
    }
}
```

It can be used with conjunction with [@MorphToField](#morphtofieldfieldname) annotation.

Note : Either default or public no-arg constructor should be present in transformer class.


## Example
```java
package com.jmorph.samaple;

import com.jmorph.annotation.FieldTransformer;
import com.jmorph.annotation.MorphTo;
import com.jmorph.annotation.MorphToField;

import java.math.BigInteger;

@MorphTo(EmployeeDTO.class)
public class EmployeeDAO {
    private String firstName;
    private String lastName;
    private BigInteger idNumber;
    private String phone;
    private String email;
    private String jobTitle;
    @MorphToField("departmentName")
    @FieldTransformer(DepartmentIdToNameTransformer.class)
    private int departmentId;
    private String supervisor;
    @MorphToField("workLocation")
    private String location;
    private int employeeType;
    private boolean isActive;
    @FieldTransformer(MillisToDateStringTransformer.class)
    private long startDate;
    @FieldTransformer(MillisToDateStringTransformer.class)
    private long endDate;
    private String documentLink;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public BigInteger getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(BigInteger idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(int employeeType) {
        this.employeeType = employeeType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(String documentLink) {
        this.documentLink = documentLink;
    }
}

```

```java
package com.jmorph.samaple;

import java.math.BigInteger;

public class EmployeeDTO {
    private String lastName;
    private String firstName;
    private BigInteger idNumber;
    private String phone;
    private String email;
    private String jobTitle;
    private String departmentName;
    private String supervisor;
    private String workLocation;
    private String employeeType;
    private String startDate;
    private String endDate;
    private String documentLink;
    private Boolean isActive;

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public BigInteger getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(BigInteger idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(String documentLink) {
        this.documentLink = documentLink;
    }
}
```

Auto generated morpher class will be like

```java
package com.jmorph.morpher;

import com.jmorph.samaple.EmployeeDAO;
import com.jmorph.samaple.EmployeeDTO;

/**
 * Auto Generate class from JMorph library. Changes made to this class will get overwritten during compiling.
 */
public final class EmployeeDAOMorpher {
    public static EmployeeDTO morph(EmployeeDAO source) {
        EmployeeDTO target = new EmployeeDTO();
        target.setLastName(source.getLastName());
        target.setFirstName(source.getFirstName());
        target.setIdNumber(source.getIdNumber());
        target.setPhone(source.getPhone());
        target.setEmail(source.getEmail());
        target.setJobTitle(source.getJobTitle());
        target.setDepartmentName(new com.jmorph.samaple.DepartmentIdToNameTransformer().transform(source.getDepartmentId()));
        target.setSupervisor(source.getSupervisor());
        target.setWorkLocation(source.getLocation());
        target.setActive(source.isActive());
        target.setStartDate(new com.jmorph.samaple.MillisToDateStringTransformer().transform(source.getStartDate()));
        target.setEndDate(new com.jmorph.samaple.MillisToDateStringTransformer().transform(source.getEndDate()));
        target.setDocumentLink(source.getDocumentLink());
        return target;
    }

    public static EmployeeDAO reverseMorph(EmployeeDTO target) {
        EmployeeDAO source = new EmployeeDAO();
        source.setLastName(target.getLastName());
        source.setFirstName(target.getFirstName());
        source.setIdNumber(target.getIdNumber());
        source.setPhone(target.getPhone());
        source.setEmail(target.getEmail());
        source.setJobTitle(target.getJobTitle());
        source.setDepartmentId(new com.jmorph.samaple.DepartmentIdToNameTransformer().reverseTransform(target.getDepartmentName()));
        source.setSupervisor(target.getSupervisor());
        source.setLocation(target.getWorkLocation());
        source.setActive(target.getActive());
        source.setStartDate(new com.jmorph.samaple.MillisToDateStringTransformer().reverseTransform(target.getStartDate()));
        source.setEndDate(new com.jmorph.samaple.MillisToDateStringTransformer().reverseTransform(target.getEndDate()));
        source.setDocumentLink(target.getDocumentLink());
        return source;
    }
}
```

## License

	Copyright 2018 Gaurang Shaha
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
		http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
