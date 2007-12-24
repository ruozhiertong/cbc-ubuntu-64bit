package net.loveruby.cflat.ast;

public class PrefixIncNode extends UnaryOpNode {
    public PrefixIncNode(Node n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
