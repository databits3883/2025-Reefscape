// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;


import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.CoolArm;
import frc.robot.subsystems.CoolArm.ArmAction;
import frc.robot.subsystems.swervedrive.SwerveSubsystem;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class AutoPickup extends Command {

  private CoolArm coolArm;
  private SwerveSubsystem drivetrain;
  private Timer pickupTimer = new Timer();
  
  /** Creates a new AutoPickup. */
  public AutoPickup(CoolArm arm,SwerveSubsystem swerve) {
    coolArm = arm;
    drivetrain = swerve;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(arm,swerve);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    coolArm.SetArmAction(ArmAction.Travel);
    pickupTimer.stop();
    pickupTimer.reset();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {

    if(coolArm.HasCoralInPickupBin()){
      
      if(!pickupTimer.isRunning()){
        
      coolArm.SetArmAction(ArmAction.Pickup);
      }
      pickupTimer.restart();
    }

    if(pickupTimer.isRunning()){
      drivetrain.setChassisSpeeds(new ChassisSpeeds(1,0,0));
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {

  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return (!coolArm.HasCoralInPickupBin() && pickupTimer.hasElapsed(0.25)) || pickupTimer.hasElapsed(2);
  }
}
