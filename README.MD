# ChromaBeam Rewrite
- [x] Basic world renderer
- [x] Component System
  - [x] Generic component primitives
  - [x] Infinite-sized 2D container
- [x] Laser Simulation
  - [x] Beam resolver and component detection
  - [x] Update scheduler and dispatcher
- [ ] Laser rendering
  - [ ] Basic rectangle-based renderer
  - [ ] (Later) line-based renderer with deferred shaders for glow effect.
- [ ] Basic input system
  - [ ] Mouse&Keyboard interaction with world
- [ ] (Temporary test) Hardcoded primitive components
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
  - This will be done with a 100% custom serializer to avoid issues with nondeterminism and importability

[![forthebadge](https://forthebadge.com/images/badges/open-source.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)
[![forthebadge](https://forthebadge.com/images/badges/designed-in-ms-paint.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/powered-by-black-magic.svg)](https://forthebadge.com)
