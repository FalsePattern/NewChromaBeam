package xyz.chromabeam.demo.components.basic;

import xyz.chromabeam.component.Component;
import xyz.chromabeam.engine.render.texture.TextureAtlas;

public class Block extends Component {

    @Override
    public void initialize(TextureAtlas atlas) {
        initialize("Block", atlas, "block");
    }
}
