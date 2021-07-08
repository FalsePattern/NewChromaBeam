package xyz.chromabeam.component.interpreter;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.BeamConsumer;
import xyz.chromabeam.component.BeamInstantManipulator;
import xyz.chromabeam.component.BeamProducer;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.component.Tickable;
import xyz.chromabeam.component.UserInteractive;
import xyz.chromabeam.world.BeamEmitter;

public class InterpreterComponent extends Component implements BeamConsumer, BeamInstantManipulator, BeamProducer, Tickable, UserInteractive {
    InterpreterComponent(String name, String id) {
        super(name, id, 0);
    }

    @Override
    public void incomingBeam(Direction direction, float red, float green, float blue) {

    }

    @Override
    public void incomingBeam(Direction direction, float red, float green, float blue, BeamEmitter instantOutput) {

    }

    @Override
    public void emitBeams(BeamEmitter beamEmitter) {

    }

    @Override
    public boolean wantEmit() {
        return false;
    }

    @Override
    public void tick() {

    }

    @Override
    public void mouseInteraction() {

    }
}
