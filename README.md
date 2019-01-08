# Timetable App

The idea of the project is to simplify the visualization and interpretation of the [curriculum](http://acs.pub.ro/~cpop/orare_sem1/) at the faculty. 

The application downloads all the available schedules from Firebase and stores them in a SQLite database on the device. Each student has to register with their group and choose their semigroup. The application displays a calendar of days with the time intervals that the student needs to be present.

## Project requirements

- Medium knowledge in Java and Object oriented programming ([good starting point](https://github.com/in28minutes/java-a-course-for-beginners))
- [Android Studio](https://developer.android.com/studio/) for development
- An Android 6.0 phone for easy debugging or HAXM supported VM
- Firebase account ([good starting point](https://firebase.google.com/))

## Project structure

- `firebase` folder contains all the necessary json files for the application to display schedules
- `app/src/main/java/mobile/computing/laurentiu/timetableapp/` is the root folder of the source code
  - `Helpers`
    - `LoaderAppCompatActivity.java` - provides loading screen functionality
    - `PlaceholderFragment.java` - provides new instances of schedules per day
    - `SectionsPagerAdapter.java` - provides the wrapper for the agenda to get schedules
  - `SQLite`
    - `DatabaseHelper.java` - provides all the necessary CRUD operations
    - `ScheduleDay.java` - model for an activity in a day
    - `ScheduleDayActivityType.java` - model for the type of activity
    - `ScheduleDayParityType.java` - model for the type of parity of activity
  - `Services`
    - `FirebaseService.java` and `IFirebaseService.java` - provides methods for reading the JSON files
    - `AgendaActivity.java` - the main activity for swiping right/left
    - `Constants.java` - helper class used for Regex patterns
    - `DoItYourselfActivity.java` - the activity displayed when a schedule is not found in storage
    - `MainActivity.java` - the main activity of the application
    - `RegisterActivity.java` - the activity in which students register with their group and choose their semigroup

## How to start the project locally

- Upload entire `firebase` folder to [Firebase Storage](https://firebase.google.com/products/storage/)
- Modify the [Constants.java](https://github.com/laurentiustamate94/timetable-app/blob/master/app/src/main/java/mobile/computing/laurentiu/timetableapp/Constants.java#L14) file and add your firebase URL (should be something like `gs://APP-ID.appspot.com`)
- Rebuild and run

## Application look&feel

### Validation

The application expects valid input. The input is validated against a Regex (this is specific to bachelor/master programmes is [my university](https://upb.ro/en/)). If the input is valid but the schedule does not exist, an activity spawns in which you navigate to this repo and contribute with the schedule that is missing.

<img src="https://github.com/laurentiustamate94/timetable-app/blob/master/images/validation.gif" height="400"> <img src="https://github.com/laurentiustamate94/timetable-app/blob/master/images/doityourself.gif" height="400">

### Schedule

After the validation is passed, the schedules are downloaded and the agenda activity remains even if you exit the application. To restart the process, you reset the registration and restart the process.

<img src="https://github.com/laurentiustamate94/timetable-app/blob/master/images/scheduledemo.gif" height="400"> <img src="https://github.com/laurentiustamate94/timetable-app/blob/master/images/scheduleremains.gif" height="400"> <img src="https://github.com/laurentiustamate94/timetable-app/blob/master/images/restartprocess.gif" height="400">

## Contribution

I'm happy to review and approve any contributions to this project. Fork, pull request and [LGTM](https://www.urbandictionary.com/define.php?term=LGTM) !
