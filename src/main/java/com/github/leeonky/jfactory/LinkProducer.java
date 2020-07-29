package com.github.leeonky.jfactory;

class LinkProducer<T> extends Producer<T> {
    private Linker<T> linker = new Linker<>();

    public static <T> LinkProducer<T> create(Producer<T> producer) {
        LinkProducer<T> linkProducer = new LinkProducer<>();
        linkProducer.linker.addProducer(producer);
        return linkProducer;
    }

    @Override
    public T produce() {
        return linker.produce();
    }

    @Override
    protected Producer<T> link(Handler<T> another) {
        return another.get().linkedByLink(another, this);
    }

    @Override
    protected Producer<T> linkedByProducer(Handler<T> another, Producer<T> producer) {
        return absorb(producer);
    }

    @Override
    protected Producer<T> linkedByLink(Handler<T> another, LinkProducer<T> linkProducer) {
        linker.merge(linkProducer.linker);
        return linkProducer;
    }

    public LinkProducer<T> absorb(Producer<T> producer) {
        linker.addProducer(producer);
        return copyLink();
    }

    public LinkProducer<T> copyLink() {
        LinkProducer<T> anotherLinkProducer = new LinkProducer<>();
        anotherLinkProducer.linker = linker;
        return anotherLinkProducer;
    }
}
