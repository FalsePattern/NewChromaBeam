# ChromaBeam Rewrite

Progress blog: https://falsepattern.moe/chromabeam/blog.html

Warning! You have to play the demo by starting it with a terminal, because input feedback
is text-based with not GUI response!.

Controls:
- R - rotate component clockwise
- Shift+R - rotate component counter-clockwise
- F - flip component(mirror)
- 0,1,2,3,4,5 - Select components
- Left click - Place component / Interact (You can click a gate to invert its switch behaviour)
- Right click - Delete component

!!!No save/load, camera movement or zooming is implemented in the demo!!!

Feature progress tracker:
- [x] Basic world renderer
- [x] Component System
  - [x] Generic component primitives
  - [x] Infinite-sized 2D container
- [x] Laser Simulation
  - [x] Beam resolver and component detection
  - [x] Update scheduler and dispatcher
  - [x] Analog lasers
- [x] Laser rendering
  - [x] line-based renderer with deferred shaders for glow effect.
- [x] Basic input system
  - [x] Mouse&Keyboard interaction with world
- [x] (Temporary test) Hardcoded primitive components
  - Hardcode the most primitive components for a proof-of-functionality release: Emitter, Mirror, Gate
- [ ] User Interface
  - [ ] Text renderer
  - [ ] Keyboard interaction with GUI
  - [ ] Mouse interaction with GUI
- [ ] Modular component system
  - Components system will be modular from the start to avoid issues with implementing mods later on.
  - [ ] Basic modular components functional
    - Emitter, Wall, Gate, Mirror, Splitter components. The minimum for working circuits.
- [ ] Audio
  - OpenAL
- [ ] Save/Load feature
  - This will be done with a 100% custom serializer to avoid issues with nondeterminism and portability

[![forthebadge](https://forthebadge.com/images/badges/open-source.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)
[![forthebadge](https://forthebadge.com/images/badges/designed-in-ms-paint.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/powered-by-black-magic.svg)](https://forthebadge.com)
