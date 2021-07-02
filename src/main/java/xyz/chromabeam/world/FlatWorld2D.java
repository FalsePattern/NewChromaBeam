package xyz.chromabeam.world;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.BeamConsumer;
import xyz.chromabeam.component.BeamInstantManipulator;
import xyz.chromabeam.component.BeamProducer;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.component.Tickable;
import xyz.chromabeam.engine.render.beam.BeamRenderer;
import xyz.chromabeam.engine.beam.Beam;
import xyz.chromabeam.util.Cache;
import xyz.chromabeam.util.storage.Container2D;
import xyz.chromabeam.util.storage.NativeContainer2D;
import xyz.chromabeam.util.storage.BeamMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FlatWorld2D implements World2D, BeamResolver {
    private final Container2D<ComponentTransform<Component>> storage = new NativeContainer2D<ComponentTransform<Component>>(ComponentTransform[]::new, ComponentTransform[][]::new, ComponentTransform[][][]::new);
    private final BeamMap beamMap = new BeamMap();

    private final Cache<Beam> propagatingBeams = new Cache<>(() -> null, Beam[]::new);
    private final Set<Beam> consumingBeams = new HashSet<>();

    private final Cache<ComponentTransform<Component>> transformCache = new Cache<ComponentTransform<Component>>(ComponentTransform::new, ComponentTransform[]::new);
    private final List<ComponentTransform<Tickable>> tickables = new ArrayList<>();
    private final List<ComponentTransform<BeamProducer>> producers = new ArrayList<>();

    private final WorldRenderer worldRenderer;
    private final BeamRenderer beamRenderer;

    public FlatWorld2D(WorldRenderer worldRenderer, BeamRenderer beamRenderer) {
        this.worldRenderer = worldRenderer;
        this.beamRenderer = beamRenderer;
    }

    @Override
    public Component set(int x, int y, Direction direction, boolean flipped, Component component) {
        if (component == null) return remove(x, y);
        var old = storage.get(x, y);
        if (old == null) {
            var tf = transformCache.getOrCreate();
            tf.with(component, x, y, direction, flipped);
            if (component.isProducer()) {
                producers.add((ComponentTransform)tf);
            }
            storage.set(x, y, tf);
            beamMap.setup(x, y);
            var n = beamMap.get(x, y);
            for (int i = 0; i < 4; i++) {
                var beam = n[i];
                if (!beam.infinite) {
                    var comp = storage.get(beam.x, beam.y).component;
                    if (comp.isConsumer() || comp.isInstantManipulator()) {
                        propagatingBeams.put(beam);
                    }
                    var opposite = beamMap.get(beam.x, beam.y, Direction.values()[(i + 2) % 4]);
                    if (opposite != null) {
                        if (component.isConsumer() || component.isInstantManipulator()) {
                            propagatingBeams.put(opposite);
                        }
                        if (beamRenderer != null) beamRenderer.drawBeam(opposite);
                    }
                }
                if (beamRenderer != null) beamRenderer.drawBeam(beam);
            }
            if (worldRenderer != null) worldRenderer.set(tf);
            return null;
        } else {
            var oldComp = old.component;
            old.with(component, x, y, direction, flipped);
            if (component.isProducer() && !producers.contains(old)) {
                producers.add((ComponentTransform)old);
            }
            if (worldRenderer != null) worldRenderer.set(old);
            return oldComp;
        }
    }

    @Override
    public Component get(int x, int y) {
        var p = storage.get(x, y);
        return p != null ? p.component : null;
    }


    @Override
    public Component remove(int x, int y) {
        var old = storage.remove(x, y);
        if (old == null) {
            return null;
        } else {
            var n = new Beam[]{new Beam(), new Beam(), new Beam(), new Beam()};
            {
                var m = beamMap.get(x, y);
                for (int i = 0; i < 4; i++) {
                    m[i].copyTo(n[i]);
                }
            }
            var dirs = new Beam[]{
                    n[0].infinite ? null : beamMap.get(n[0].x, n[0].y, Direction.LEFT),
                    n[1].infinite ? null : beamMap.get(n[1].x, n[1].y, Direction.UP),
                    n[2].infinite ? null : beamMap.get(n[2].x, n[2].y, Direction.RIGHT),
                    n[3].infinite ? null : beamMap.get(n[3].x, n[3].y, Direction.DOWN)
            };
            beamMap.removeAll(x, y);

            for (int i = 0; i < 4; i++) {
                if (dirs[i] != null && dirs[(i + 2) % 4] == null) {
                    dirs[i].infinite = true;
                    switch (i) {
                        case 0 -> dirs[i].x = Integer.MIN_VALUE;
                        case 1 -> dirs[i].y = Integer.MIN_VALUE;
                        case 2 -> dirs[i].x = Integer.MAX_VALUE;
                        case 3 -> dirs[i].y = Integer.MAX_VALUE;
                    }
                }
                if (dirs[i] != null) {
                    var beam = dirs[i];
                    var target = storage.get(beam.x, beam.y);
                    if (target != null) {
                        if (target.component.isInstantManipulator() || target.component.isConsumer()) {
                            propagatingBeams.put(beam);
                        }
                    }
                } else {
                    var beam = n[(i + 2) % 4];
                    var target = storage.get(beam.x, beam.y);
                    if (target != null && (target.component.isInstantManipulator() || target.component.isConsumer())) {
                        var copy = new Beam();
                        beam.copyTo(copy);
                        beam.red = 0;
                        beam.green = 0;
                        beam.blue = 0;
                        propagatingBeams.put(beam);
                    }
                }
            }
            if (beamRenderer != null) {
                beamRenderer.removeAll(x, y);
                for (int i = 0; i < 4; i++) {
                    if (dirs[i] != null) {
                        beamRenderer.drawBeam(dirs[i]);
                    }
                }
            }
            var comp = old.component;
            old.component = null;
            producers.remove(old);
            transformCache.put(old);
            if (worldRenderer != null) worldRenderer.remove(x, y);
            return comp;
        }
    }

    private final ComponentTransform<Component> beamComponentBuffer = new ComponentTransform<>();
    @Override
    public void update() {
        for (var producer: producers) {
            producer.component.emitBeams((direction, red, green, blue) -> scheduleBeam(producer.position.x, producer.position.y, direction.applyFlip(producer.flipped).add(producer.direction), red, green, blue));
        }
        producers.clear();
        while (!propagatingBeams.empty()) {
            var beam = propagatingBeams.get();
            var transform = storage.get(beam.x, beam.y);
            if (transform.component.isInstantManipulator()) {
                ((BeamInstantManipulator) transform.component).incomingBeam(beam.direction.sub(transform.direction).applyFlip(transform.flipped), beam.red, beam.green, beam.blue,
                        (direction, red, green, blue) -> scheduleBeam(transform.position.x, transform.position.y, direction.applyFlip(transform.flipped).add(transform.direction), red, green, blue));
            }
            if (transform.component.isConsumer()) {
                consumingBeams.add(beam);
            }
        }
        for (var beam: consumingBeams) {
            var transform = storage.get(beam.x, beam.y);
            ((BeamConsumer)transform.component).incomingBeam(beam.direction.sub(transform.direction).applyFlip(transform.flipped), beam.red, beam.green, beam.blue);
            tickables.add((ComponentTransform)transform);
        }
        consumingBeams.clear();

        for (var tickable: tickables) {
            tick(tickable);
        }
        tickables.clear();
    }

    private void tick(ComponentTransform<Tickable> tickable) {
        tickable.component.tick();
        if (tickable.component.isGraphicsChanged()) {
            tickable.component.updateGraphics();
            worldRenderer.set((ComponentTransform) tickable);
        }
        if (tickable.component instanceof BeamProducer p && p.wantEmit()) {
            producers.add((ComponentTransform)tickable);
        }
    }

    @Override
    public void forceTick(int x, int y) {
        tick((ComponentTransform)storage.get(x, y));
    }

    @Override
    public ComponentTransform<Component> getTransform(int x, int y, ComponentTransform<Component> buffer) {
        return buffer.with(storage.get(x, y));
    }

    @Override
    public void scheduleBeam(int x, int y, Direction direction, float red, float green, float blue) {
        var beam = beamMap.get(x, y, direction);
        beam.red = red;
        beam.green = green;
        beam.blue = blue;
        if (beam.infinite) {
            if (beamRenderer != null) {
                beamRenderer.drawBeam(beam);
            }
        } else {
            var target = storage.get(beam.x, beam.y).component;
            if (target.isInstantManipulator() || target.isConsumer()) {
                propagatingBeams.put(beam);
            }
            if (beamRenderer != null) beamRenderer.drawBeam(beam);
        }
    }

}
