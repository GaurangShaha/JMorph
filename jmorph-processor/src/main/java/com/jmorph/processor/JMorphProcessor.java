package com.jmorph.processor;

import com.jmorph.annotation.MorphTo;
import com.jmorph.processor.exception.ValidationException;
import com.jmorph.processor.util.ElementUtil;
import com.jmorph.processor.util.JavaPoetUtil;
import com.jmorph.processor.util.ValidationUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class JMorphProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        processMorphToAnnotatedElements(roundEnvironment);
        return true;
    }

    private void processMorphToAnnotatedElements(RoundEnvironment roundEnvironment) {
        for (Element sourceClassElement : roundEnvironment.getElementsAnnotatedWith(MorphTo.class)) {
            try {
                if (ValidationUtil.isValidElementForMorphToAnnotation(sourceClassElement)) {
                    TypeElement targetClassElement = ElementUtil.getTypeElement(ElementUtil.getAnnotationValue(ElementUtil.getAnnotationMirror(sourceClassElement, MorphTo.class), null), typeUtils);

                    if (ValidationUtil.isValidElementForMorphToAnnotation(targetClassElement)) {
                        generateMorpher((TypeElement) sourceClassElement, targetClassElement);
                    }
                }
            } catch (ValidationException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getError());
            }
        }
    }

    private void generateMorpher(TypeElement sourceClassElement, TypeElement targetClassElement) {
        try {
            JavaPoetUtil.createMorpherFile(ElementUtil.generateMorpherMetaData(sourceClassElement, targetClassElement, processingEnv.getTypeUtils()), filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        } catch (ValidationException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getError());
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(MorphTo.class.getCanonicalName());
        return annotations;
    }
}
