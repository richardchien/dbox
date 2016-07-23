# DBox for Android

[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](LICENSE)
[![Release](https://jitpack.io/v/richardchien/dbox-android.svg)](https://jitpack.io/#richardchien/dbox-android)

[中文](#zh) [English](#en)

<a name="zh">

用于 Android 的轻量级 ORM 库，支持 boolean、byte、short、int、long、float、double、String、Date、byte[] 基本类型，以及 Model 对象之间一对一、一对多、多对一、多对多的关系。使用比较简单，不过性能可能不是很好（其实我还没测过性能），尤其在一个 Model 对象中又包含其它 Model 对象的情况比较多时，涉及到较多的跨表查询，不过 DBox 也支持懒加载，可以减轻一些性能上的问题。

因为到目前为止，这个项目还并没有很成熟，所以可能存在各种未知的 Bug，请不要再生产环境中使用，如果你在使用中遇到了任何问题，请发 Issue 或 PR，我们共同改进。

Wiki：[https://github.com/richardchien/dbox-android/wiki](https://github.com/richardchien/dbox-android/wiki)

API 文档：[http://richardchien.github.io/dbox-android/](http://richardchien.github.io/dbox-android/)

## 基本用法

### 添加 Gradle 依赖

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    compile 'com.github.richardchien:dbox-android:v1.0.0-beta2'
}
```

### 创建 Model 类

例如：

```java
@Table("Student")
class Student {
    private long id;

    @Column(notNull = true)
    String name;

    @ObjectColumn(Course.class)
    List<Course> courseList;

    @ObjectColumn(Course.class)
    Course[] favoriteCourses;

    @ObjectColumn(Clazz.class)
    Clazz clazz;

    Student() {
        courseList = new ArrayList<>();
    }

    long getId() {
        return id;
    }
}
```

用 `@Table` 注解来将类标注为可识别的 Model 类，默认会使用类名作为表名，也可以在注解参数中设置。

每个 Model 类必须有一个名为 `id` 的、类型为 `long` 的字段，并且不要手动修改它的值。其它需要存储到数据库的字段，用 `@Column` 或 `@ObjectColumn` 注解标注，其中，用 `@Column` 标注基本数据类型的字段，并可在参数中设置是否主键、是否非空等；用 `@ObjectColumn` 标注其它 Model 对象类型或其类型的数组或 List 的字段，注解参数需要填该 Model 类型的 class 对象。注意 Model 对象之间的关系不可以发生递归，即，不可以 ModelA 包含一个 ModelB 类型的字段同时 ModelB 包含一个 ModelA 类型的字段，否则将出现不可预知的错误。

受限于表结构，除了 id 字段外，还应当至少有一个基本类型的字段，否则无法正常存储。并且，id 字段不需要使用 `@Column` 标注。

另外，每个 Model 类必须有一个参数为空的构造函数，如果没有添加其它构造函数，则 Java 默认会添加一个空的构造函数，保持默认即可。

### 保存、更新、删除对象

```java
// 保存
Clazz clz = new Clazz("Class 1");
DBox.of(Clazz.class).save(clz);

Course crs = new Course("C101", "Course 1");
DBox.of(Course.class).save(crs);

Student stu = new Student();
stu.clazz = clz;
stu.favoriteCourses = new Course[]{crs};
stu.courseList.add(crs);
DBox.of(Student.class).save(stu);

// 更新
clz.name = "Class 2";
DBox.of(Clazz.class).save(clz);

// 删除
DBox.of(Student.class).remove(stu);
```

包含在其它 Model 对象里的 Model 对象需要先保存。并且，调用 `save()` 之后，Model 对象的 id 字段将被设置为新插入的记录的 id 值，在修改了其他字段后，再次保存时，会根据这个 id 来更新相应的表。

### 查询对象

```java
List<Student> list = DBox.of(Student.class)
        .find(new DBoxCondition()
                .between("id", "10", "20")
                .or()
                .equalTo("name", "Richard"))
        .orderByDesc("name")
        .results()
        .all();

// 或使用懒加载
DBoxResults<Student> results = DBox.of(Student.class)
        .find(new DBoxCondition()
                .between("id", "10", "20")
                .or()
                .equalTo("name", "Richard"))
        .orderByDesc("name")
        .results();
for (Student stu : results) {
    // Do somthing
}
```

查询对象可以用 `findAll()` 查询所有对象，或 `find()` 来根据条件查找，后者需要传入一个 `DBoxCondition` 对象作参数。`DBoxCondition` 对象实际上会被解析成 WHERE 语句的一部分，注意在构造 `DBoxCondition` 对象时，第一个参数只能传入要查询的 Model 类的基本类型的字段名，并且如果在 `@Column` 中自定义了字段名，需要使用自定义的字段名，`orderBy()` 和 `orderByDesc()` 也一样。

## 更多资料

详细文档请查阅 [Wiki](https://github.com/richardchien/dbox-android/wiki) 和 [API 文档](http://richardchien.github.io/dbox-android/)。

## 另

> 我用代码写成诗
>
> 以你命名
>
> 只为祈祷
>
> 你会永远伴我同行
>
> ——某腊鸡

---------

<a name="en">

An ORM framework for Android, supporting both basic types like boolean, byte, short, int, long, float, double, String, Date, byte[], and complex relationships among different model objects. It also supports lazy loading.

Since this framework is not quite stable yet, any unknown bugs could be observed. Don't use this in production environment. If any problems occur to you, issues or PRs are welcome.

Wiki：[https://github.com/richardchien/dbox-android/wiki](https://github.com/richardchien/dbox-android/wiki)

API docs：[http://richardchien.github.io/dbox-android/](http://richardchien.github.io/dbox-android/)

## Basic Usage

### Add Gradle dependency

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    compile 'com.github.richardchien:dbox-android:v1.0.0-beta2'
}
```

### Create model class

Example:

```java
@Table("Student")
class Student {
    private long id;

    @Column(notNull = true)
    String name;

    @ObjectColumn(Course.class)
    List<Course> courseList;

    @ObjectColumn(Course.class)
    Course[] favoriteCourses;

    @ObjectColumn(Clazz.class)
    Clazz clazz;

    Student() {
        courseList = new ArrayList<>();
    }

    long getId() {
        return id;
    }
}
```

Use `@Table` annotation to mark a class as a recognizable model class. DBox will use the class name as table name by default, you can also custom it through parameter of annotation by yourself.

There must be a field named `id` and of type `long`, and its value shouldn't be set manually. Use `@Column` annotation to mark a field as a column of basic data types and make it "unique" or "not null" or something else through annotation parameters. Or, use `@ObjectColumn` annotation to mark a field of other model class or array or list of other model class, and give it the model class's class object as parameter. Note that model class can't contain each other recursively, otherwise, the result is undetermined.

Be limited by the structure of tables, there should be at least one field of basic types besides the id field. And, the id field don't need to be marked with `@Column` annotation.

In addition, every model class should have a constructor with empty parameter list.

### Save、update、remove

```java
// Save
Clazz clz = new Clazz("Class 1");
DBox.of(Clazz.class).save(clz);

Course crs = new Course("C101", "Course 1");
DBox.of(Course.class).save(crs);

Student stu = new Student();
stu.clazz = clz;
stu.favoriteCourses = new Course[]{crs};
stu.courseList.add(crs);
DBox.of(Student.class).save(stu);

// Update
clz.name = "Class 2";
DBox.of(Clazz.class).save(clz);

// Remove
DBox.of(Student.class).remove(stu);
```

The model object contained by another should be saved first. After `save()` being called, the id field of the model object will be set. When you re-save (update) the object, DBox will update the corresponding table according to that id.

### Find objects

```java
List<Student> list = DBox.of(Student.class)
        .find(new DBoxCondition()
                .between("id", "10", "20")
                .or()
                .equalTo("name", "Richard"))
        .orderByDesc("name")
        .results()
        .all();

// Or use lazy loading
DBoxResults<Student> results = DBox.of(Student.class)
        .find(new DBoxCondition()
                .between("id", "10", "20")
                .or()
                .equalTo("name", "Richard"))
        .orderByDesc("name")
        .results();
for (Student stu : results) {
    // Do somthing
}
```

You can use `findAll()` to find all objects or use `find()` to find some specific objects that satisfy the condition you passed in. The `DBoxCondition` object will become part of the WHERE clause in fact. Note that while building a condition object, you can only pass column names in the model class (if you didn't customize the column name in `@Column` annotation, the column name is the same as field name) and so are `orderBy()` 和 `orderByDesc()`.

## More Information

Please check [Wiki](https://github.com/richardchien/dbox-android/wiki) and [API docs](http://richardchien.github.io/dbox-android/) for more information.

## BTW

> I wrote code into poet
>
> and name it after you
>
> praying
>
> You will forever stand by me
>
> — RC
