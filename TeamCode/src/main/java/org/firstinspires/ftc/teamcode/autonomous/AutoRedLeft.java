package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name="Robot: Auto Red Left", group="Robot")
//@Disabled
public class AutoRedLeft extends LinearOpMode {
    // COUNTS_PER_INCH is the conversion multiplier. Multiply by an inch count,
    // and it will convert to the same encoder count. (~538 counts is one rotation)
    static final double WHEEL_DIAMETER_INCHES = 3.779528;
    static final double COUNTS_PER_INCH = 537.6898395722 / WHEEL_DIAMETER_INCHES / 3.1415;
    static final double STRAFE_COUNTS_PER_INCH = 537 / 10.3775;
    static final double TURN_COUNTS_PER_DEGREE = 3836.0 / 360;

    // Define the motors
    private DcMotor rightFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor leftFrontDrive = null;
    private DcMotor rightBackDrive = null;

    private DcMotor spinMotor = null;
    private DcMotor extendMotor = null;
    private Servo servoTilt = null;
    private Servo servoRelease = null;

    final double MAX_POS_TILT     =  0.73;     // Maximum rotational position
    final double MIN_POS_TILT     =  0.413;     // Minimum rotational position
    final double MAX_POS_RELEASE = 0.4;    // Highest position (all pixels released)
    final double MID_POS_RELEASE = 0.36;     // Middle position (1 pixel released)
    final double MIN_POS_RELEASE = 0.31;   // Lowest position (default)


    // Telemetry and timer
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        // As of 10.31.23 the following order matches the order
        // the motors are plugged into the ports (0-3)
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");

        // Servos, intake, and slide motors
        spinMotor = hardwareMap.get(DcMotor.class, "spin_motor");
        extendMotor = hardwareMap.get(DcMotor.class, "extend_motor");
        servoTilt = hardwareMap.get(Servo.class, "servo_motor");
        servoRelease = hardwareMap.get(Servo.class, "servo_release");

        // Set directions
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        resetEncoders();

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        extendMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Give phone an update
        telemetry.addData("Status", "'Dis piece o' s*** is READY baby.");
        telemetry.update();

        waitForStart(); // Waiting for start button
        runtime.reset();

        // Reset servos
        servoTilt.setPosition(MIN_POS_TILT);
        servoRelease.setPosition(MIN_POS_RELEASE);

        ///////// AUTO COMMANDS \\\\\\\\\\
        encoderDrive(8, 8, .3);
        encoderStrafe(3, .2);
        encoderDrive(20, 20, .3);     // Drive and push over game piece if in the way
        encoderDrive(-1, -1, .3);
        spinMotor.setPower(-.3);                        // Spit out pixel
        encoderDrive(-12, -12, .3);   // Backup to make sure it's out
        spinMotor.setPower(0);

        encoderStrafe(-20, .3);            // Get to middle of field
        encoderDrive(33, 33, .3);
        encoderTurn(-90, .2);

        encoderDrive(-102, -102, .4);          // go across the field
        encoderStrafe(-26, .3);  // Align with the board
        encoderDrive(-6, -6, .2);   // Back up a tiny bit

        placePixel(940, .7);           // Score! (hopefully)

        encoderStrafe(22, .4);          // Park and finish
        //////////  END OF AUTONOMOUS  \\\\\\\\\


        // Let drive know when finished
        telemetry.addData("Status", "'Tis Complete, your Majesty");
        telemetry.addData("Duration", runtime.toString());
        telemetry.update();

        sleep(5000); // Give 5 sec to view message before finishing
    }

    private void placePixel(int height, double slideSpeed) {
        // This function extends the slides and drops a pixel onto the board
        // First, move slide to a desired height
        extendMotor.setTargetPosition(-height); // slide moves in negative direction
        extendMotor.setPower(slideSpeed);
        extendMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        sleep(1000);
        // Release pixel
        servoTilt.setPosition(MAX_POS_TILT);
        sleep(500);
        servoRelease.setPosition(MAX_POS_RELEASE);
        sleep(500);
        // Reset
        servoTilt.setPosition(MIN_POS_TILT);
        servoRelease.setPosition(MIN_POS_RELEASE);
        sleep(300);
        extendMotor.setTargetPosition(0);
        sleep(1000);
        extendMotor.setPower(0);        // (Turn off slide)
        extendMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void resetEncoders() {
        // Reset encoders and set their encoder mode
        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        extendMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public void encoderDrive(double leftIn, double rightIn, double speed) {
        // The juicy encoder details will be in here
        int leftBackTarget;
        int leftFrontTarget;
        int rightBackTarget;
        int rightFrontTarget;

        if (opModeIsActive()) {
            // Convert inputted inches into encoder counts
            // Also, set the target encoder counts per motor
            resetEncoders();
            leftBackTarget = (int)(leftIn * COUNTS_PER_INCH);
            leftBackDrive.setTargetPosition(leftBackTarget);
            leftFrontTarget = (int)(leftIn * COUNTS_PER_INCH);
            leftFrontDrive.setTargetPosition(leftFrontTarget);
            rightBackTarget = (int)(rightIn * COUNTS_PER_INCH);
            rightBackDrive.setTargetPosition(rightBackTarget);
            rightFrontTarget = (int)(rightIn * COUNTS_PER_INCH);
            rightFrontDrive.setTargetPosition(rightFrontTarget);

            // Run the robot auto with the new encoder targets
            startWaitFinish(speed, 250);
        }
    }

    public void encoderStrafe(double targetIn, double speed) {
        // This func will move the robot to a desired inches target
        // Right is positive inches, left is negative
        int leftBackTarget;
        int leftFrontTarget;
        int rightBackTarget;
        int rightFrontTarget;

        if (opModeIsActive()) {
            // Set targets using conversion factor to turn inches into encoder counts
            resetEncoders();
            double newTarget = (targetIn * STRAFE_COUNTS_PER_INCH);

            rightFrontTarget = -(int)newTarget;
            rightFrontDrive.setTargetPosition(rightFrontTarget);
            leftBackTarget = -(int)newTarget;
            leftBackDrive.setTargetPosition(leftBackTarget);
            rightBackTarget = (int)newTarget;
            rightBackDrive.setTargetPosition(rightBackTarget);
            leftFrontTarget = (int)newTarget;
            leftFrontDrive.setTargetPosition(leftFrontTarget);

            // Run
            startWaitFinish(speed, 250);
        }
    }

    public void encoderTurn(double angle, double speed) {
        // This function will turn the robot to an exact angle
        // to the best ability of the GoBilda encoders.
        int leftBackTarget;
        int leftFrontTarget;
        int rightBackTarget;
        int rightFrontTarget;

        if (opModeIsActive()) {
            resetEncoders();
            int newTarget = (int)(angle * TURN_COUNTS_PER_DEGREE);

            rightFrontTarget = -newTarget;
            rightFrontDrive.setTargetPosition(rightFrontTarget);
            leftBackTarget = newTarget;
            leftBackDrive.setTargetPosition(leftBackTarget);
            rightBackTarget = -newTarget;
            rightBackDrive.setTargetPosition(rightBackTarget);
            leftFrontTarget = newTarget;
            leftFrontDrive.setTargetPosition(leftFrontTarget);

            // Run
            startWaitFinish(speed, 250);
        }
    }

    public void startWaitFinish(double speed, long waitMillis) {
        // This code is reused a couple times, so all it does is turn on
        // the motors (after their encoder targets are set) and wait until
        // they are done running to their position.
        // Turn on the motors
        leftBackDrive.setPower(Math.abs(speed));
        leftFrontDrive.setPower(Math.abs(speed));
        rightBackDrive.setPower(Math.abs(speed));
        rightFrontDrive.setPower(Math.abs(speed));

        // Set encoder mode to run
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // The movement will automatically stop if the runtime exceeds
        // 120 seconds (length of an whole ftc match).
        // Don't want the robot to run forever by accident
        while (opModeIsActive() &&
                leftFrontDrive.isBusy() || leftBackDrive.isBusy() &&
                rightFrontDrive.isBusy() && rightBackDrive.isBusy() &&
                runtime.seconds() < 120) {
            telemetry.addData("Status", "Busy af rn leave me alone");
            telemetry.addData("Positions (lf, lb, rf, rb)", "%7d; %7d; %7d; %7d",
                    leftFrontDrive.getCurrentPosition(),
                    leftBackDrive.getCurrentPosition(),
                    rightFrontDrive.getCurrentPosition(),
                    rightBackDrive.getCurrentPosition());
            telemetry.update();
        }

        // Turn off motors
        leftFrontDrive.setPower(0);
        leftBackDrive.setPower(0);
        rightFrontDrive.setPower(0);
        rightBackDrive.setPower(0);

        // This will turn off the "RUN_TO_POSITION" mode on each motor
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Finished move
        telemetry.addData("Status", "Move finished");
        telemetry.update();
        sleep(waitMillis); // Wait a second before next move
    }
}
