package com.laefye.libraryofbabel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jglrxavpok.hephaistos.nbt.NBT;

public class Library implements Generator {
    @Override
    public void generate(GenerationUnit unit) {
        Point start = unit.absoluteStart();
        Point size = unit.size();
        var serialize = GsonComponentSerializer.gson();
        for (int x = 0; x < size.blockX(); x++) {
            for (int z = 0; z < size.blockZ(); z++) {
                for (int y = 0; y < size.blockY(); y++) {
                    var pos = Pos.fromPoint(start.add(x, y, z));
                    if (!WordPos.isValid(pos))
                    {
                        continue;
                    }
                    var wordBegin = WordPos.of(pos, 0, 0);
                    var wordEnd = WordPos.of(pos, WordPos.BOOK_MASK.intValue(), WordPos.PAGE_MASK.intValue());
                    unit.modifier().setBlock(start.add(x, y,  z), Block.BOOKSHELF);
                    unit.modifier().setBlock(start.add(x, y+1,  z), Block.OAK_SIGN.withNbt(NBT.Compound(compound -> {
                        compound.put("Text1", NBT.String(
                                serialize.serialize(Component.text(wordBegin.getSeed().toString(Character.MAX_RADIX)))
                        ));
                        compound.put("Text2", NBT.String(
                                serialize.serialize(Component.text("-"))
                        ));
                        compound.put("Text3", NBT.String(
                                serialize.serialize(Component.text(wordEnd.getSeed().toString(Character.MAX_RADIX)))
                        ));
                    })));
                }
            }
        }
    }
}
