// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.VisionConstants;
import frc.robot.commands.swervedrive.drivebase.AbsoluteDriveAdv;
import frc.robot.subsystems.CoolArm;
import frc.robot.subsystems.swervedrive.SwerveSubsystem;
import frc.robot.subsystems.swervedrive.Vision;

import java.io.File;
import java.util.function.DoubleSupplier;

import swervelib.SwerveInputStream;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a "declarative" paradigm, very
 * little robot logic should actually be handled in the {@link Robot} periodic methods (other than the scheduler calls).
 * Instead, the structure of the robot (including subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer
{

  // Replace with CommandPS4Controller or CommandJoystick if needed
  final         CommandJoystick driverJoystick = new CommandJoystick(0);

  final         CommandJoystick copilotController = new CommandJoystick(1);

  // The robot's subsystems and commands are defined here...
  private final SwerveSubsystem       drivebase  = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(),
                                                                                "swerve/Omega"));
  private final Vision vision = drivebase.vision;

  private final CoolArm coolArm = new CoolArm();
  // Applies deadbands and inverts controls because joysticks
  // are back-right positive while robot
  // controls are front-left positive
  // left stick controls translation
  // right stick controls the rotational velocity 
  // buttons are quick rotation positions to different ways to face
  // WARNING: default buttons are on the same buttons as the ones defined in configureBindings

  // AbsoluteDriveAdv closedAbsoluteDriveAdv = new AbsoluteDriveAdv(drivebase,
  //                                                                () -> -MathUtil.applyDeadband(driverXbox.getLeftY(),
  //                                                                                              OperatorConstants.LEFT_Y_DEADBAND),
  //                                                                () -> -MathUtil.applyDeadband(driverXbox.getLeftX(),
  //                                                                                              OperatorConstants.DEADBAND),
  //                                                                () -> -MathUtil.applyDeadband(driverXbox.getRightX(),
  //                                                                                              OperatorConstants.RIGHT_X_DEADBAND),
  //                                                                driverXbox.getHID()::getYButtonPressed,
  //                                                                driverXbox.getHID()::getAButtonPressed,
  //                                                                driverXbox.getHID()::getXButtonPressed,
  //                                                                driverXbox.getHID()::getBButtonPressed);

  /**
   * Converts driver input into a field-relative ChassisSpeeds that is controlled by angular velocity.
   */
  SwerveInputStream driveAngularVelocity = SwerveInputStream.of(drivebase.getSwerveDrive(),
                                                                () -> driverJoystick.getY() * -1,
                                                                () -> driverJoystick.getX() * -1)
                                                            .withControllerRotationAxis(() -> driverJoystick.getTwist() * -0.5)
                                                            .deadband(OperatorConstants.DEADBAND)
                                                            .scaleTranslation(0.8)
                                                            .allianceRelativeControl(true);

  /**
   * Clone's the angular velocity input stream and converts it to a fieldRelative input stream.
   */
  // SwerveInputStream driveDirectAngle = driveAngularVelocity.copy().withControllerHeadingAxis(driverXbox::getRightX,
  //                                                                                            driverXbox::getRightY)
  //                                                          .headingWhile(true);


  // Applies deadbands and inverts controls because joysticks
  // are back-right positive while robot
  // controls are front-left positive
  // left stick controls translation
  // right stick controls the desired angle NOT angular rotation
  //Command driveFieldOrientedDirectAngle = drivebase.driveFieldOriented(driveAngularVelocity);

  // Applies deadbands and inverts controls because joysticks
  // are back-right positive while robot
  // controls are front-left positive
  // left stick controls translation
  // right stick controls the angular velocity of the robot
  Command driveFieldOrientedAnglularVelocity = drivebase.driveFieldOriented(driveAngularVelocity);

  Command driveSetpointGen = drivebase.driveWithSetpointGeneratorFieldRelative(driveAngularVelocity);

  SwerveInputStream driveAngularVelocitySim = SwerveInputStream.of(drivebase.getSwerveDrive(),
                                                                   () -> -driverJoystick.getY(),
                                                                   () -> -driverJoystick.getX())
                                                               .withControllerRotationAxis(() -> driverJoystick.getRawAxis(2))
                                                               .deadband(OperatorConstants.DEADBAND)
                                                               .scaleTranslation(0.8)
                                                               .allianceRelativeControl(true);
  // Derive the heading axis with math!
  SwerveInputStream driveDirectAngleSim     = driveAngularVelocitySim.copy()
                                                                     .withControllerHeadingAxis(() -> Math.sin(
                                                                                                    driverJoystick.getRawAxis(
                                                                                                        2) * Math.PI) * (Math.PI * 2),
                                                                                                () -> Math.cos(
                                                                                                    driverJoystick.getRawAxis(
                                                                                                        2) * Math.PI) *
                                                                                                      (Math.PI * 2))
                                                                     .headingWhile(true);

  Command driveFieldOrientedDirectAngleSim = drivebase.driveFieldOriented(driveDirectAngleSim);

  Command driveSetpointGenSim = drivebase.driveWithSetpointGeneratorFieldRelative(driveDirectAngleSim);

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer()
  {
    // Configure the trigger bindings
    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);
    NamedCommands.registerCommand("test", Commands.print("I EXIST"));

    // Shuffleboard.getTab("testing").addDouble("Distance to Tag 16", vision::getLatestTag16Distance);
    // driverJoystick.button(14).onTrue(new InstantCommand( () -> this.getTag16Distance()  )) ;
    //driverJoystick.button(14).onTrue(new InstantCommand( () -> drivebase.()  )) ;
  }

  public void getTag16Distance(){
    if(vision != null){
      System.out.println(vision.getBestTargetDistanceToCamera());
      
    }
    
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary predicate, or via the
   * named factories in {@link edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for
   * {@link CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller PS4}
   * controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight joysticks}.
   */
  private void configureBindings()
  {
    // (Condition) ? Return-On-True : Return-on-False
    drivebase.setDefaultCommand(driveFieldOrientedAnglularVelocity);

    driverJoystick.button(13).onTrue(Commands.runOnce(drivebase::zeroGyro));

    Command autoAim = drivebase.aimAtSpeaker(5);
    autoAim.addRequirements(drivebase);
    driverJoystick.button(3).whileTrue(autoAim);


    driverJoystick.button(6).whileTrue(new StartEndCommand(() -> coolArm.SetAngleMotor(0.1), () -> coolArm.SetAngleMotor(0), coolArm));
    driverJoystick.button(9).whileTrue(new StartEndCommand(() -> coolArm.SetAngleMotor(-0.1), () -> coolArm.SetAngleMotor(0), coolArm));

    // driverJoystick.button(5).whileTrue(new StartEndCommand(() -> coolArm.SetElevatorMotor(0.1), () -> coolArm.SetElevatorMotor(0), coolArm));
    // driverJoystick.button(8).whileTrue(new StartEndCommand(() -> coolArm.SetElevatorMotor(-0.1), () -> coolArm.SetElevatorMotor(0), coolArm));

    // driverJoystick.button(15).whileTrue(coolArm.sysIdRoutine.dynamic(Direction.kForward));
    // driverJoystick.button(12).whileTrue(coolArm.sysIdRoutine.quasistatic(Direction.kForward));

    // driverJoystick.button(16).whileTrue(coolArm.sysIdRoutine.dynamic(Direction.kReverse));
    // driverJoystick.button(11).whileTrue(coolArm.sysIdRoutine.quasistatic(Direction.kReverse));

    //coolArm.setDefaultCommand(new RunCommand(() -> coolArm.SetAngleSetpoint(driverJoystick.getThrottle() *-90 + 180),coolArm));

    copilotController.button(1).onTrue(new InstantCommand(()->coolArm.SetArmAction(CoolArm.ArmAction.Place)));
    copilotController.button(6).onTrue(new InstantCommand(()->coolArm.SetArmAction(CoolArm.ArmAction.L1)));
    copilotController.button(7).onTrue(new InstantCommand(()->coolArm.SetArmAction(CoolArm.ArmAction.L2)));
    copilotController.button(11).onTrue(new InstantCommand(()->coolArm.SetArmAction(CoolArm.ArmAction.L3)));
    copilotController.button(10).onTrue(new InstantCommand(()->coolArm.SetArmAction(CoolArm.ArmAction.L4)));

    copilotController.button(8).onTrue(new InstantCommand(()->coolArm.SetArmAction(CoolArm.ArmAction.Travel)));
    copilotController.button(9).onTrue(new InstantCommand(()->coolArm.SetArmAction(CoolArm.ArmAction.Pickup)));

    copilotController.button(3).whileTrue(new StartEndCommand(()->coolArm.SetElevatorMotorManual(1), ()->coolArm.SetElevatorMotor(0),coolArm));
    copilotController.button(2).whileTrue(new StartEndCommand(()->coolArm.SetElevatorMotorManual(-1),()->coolArm.SetElevatorMotor(0),coolArm));
    copilotController.button(4).onTrue(new InstantCommand(()-> coolArm.SetElevatorEncoderPosition(0) , coolArm));

    driverJoystick.button(4).whileTrue(drivebase.driveToPose(VisionConstants.kReefGoalPoses[18][0].toPose2d()).alongWith(new PrintCommand("X: "+ VisionConstants.kReefGoalPoses[18][0].getX() + "Y: "+ VisionConstants.kReefGoalPoses[18][0].getY() + "Z: "+ VisionConstants.kReefGoalPoses[18][0].getZ())));
    driverJoystick.button(3).whileTrue(drivebase.driveToPose(VisionConstants.kReefGoalPoses[18][1].toPose2d()).alongWith(new PrintCommand("X: "+ VisionConstants.kReefGoalPoses[18][1].getX() + "Y: "+ VisionConstants.kReefGoalPoses[18][1].getY() + "Z: "+ VisionConstants.kReefGoalPoses[18][1].getZ())));

  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand()
  {
    // An example command will be run in autonomous
    return new InstantCommand(()->drivebase.resetOdometry(new Pose2d(0, 0, Rotation2d.fromDegrees(0))) ).alongWith(drivebase.getAutonomousCommand("1m Drive"));
  }

  public void setDriveMode()
  {
    configureBindings();
  }

  public void setMotorBrake(boolean brake)
  {
    drivebase.setMotorBrake(brake);
  }

  public void disabledPeriodic(){
    coolArm.SetAngleSetpoint(coolArm.absAngleEncoder.getPosition());
  }
}
