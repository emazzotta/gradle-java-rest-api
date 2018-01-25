/*
 * MIT License
 *
 * Copyright (c) 2016 - 2018 Silvio Wangler (silvio.wangler@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ch.silviowangler.gradle.restapi.builder.spring;

import ch.silviowangler.gradle.restapi.AnnotationTypes;
import ch.silviowangler.gradle.restapi.builder.AbstractResourceBuilder;
import ch.silviowangler.gradle.restapi.builder.ArtifactType;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.silviowangler.gradle.restapi.AnnotationTypes.*;

public class SpringRootResourceFactory extends AbstractResourceBuilder {

    public static final ClassName CLASS_NAME = ClassName.get(String.class);

    private TypeSpec.Builder resourceBuilder;

    @Override
    public TypeSpec buildRootResource() {
        reset();
        setArtifactType(ArtifactType.RESOURCE);
        resourceBuilder = interfaceBaseInstance();

        Map<String, Object> args = new HashMap<>();
        args.put("value", getPath());

        resourceBuilder.addAnnotation(createAnnotation(SPRING_REQUEST_MAPPING, args));

        generateResourceMethods();
        return resourceBuilder.build();
    }

    @Override
    public TypeSpec buildResourceImpl() {
        reset();
        setArtifactType(ArtifactType.RESOURCE_IMPL);
        TypeSpec.Builder builder = classBaseInstance();

        builder.addAnnotation(createAnnotation(SPRING_REST_CONTROLLER));

        builder.addSuperinterface(ClassName.get(getCurrentPackageName(), resourceName()));

        super.generateResourceMethods();
        return builder.build();
    }

    @Override
    protected void createOptionsMethod() {

        Verb verb = new Verb();
        verb.setVerb("OPTIONS");

        setCurrentVerb(verb);
        MethodSpec.Builder optionsMethod = createMethod("getOptions", CLASS_NAME);

        optionsMethod.addStatement("return OPTIONS_CONTENT");

        interfaceBaseInstance().addMethod(optionsMethod.build());
        setCurrentVerb(null);
    }

    @Override
    public AnnotationSpec getQueryParamAnnotation(String paramName) {
        return AnnotationSpec.builder(SPRING_REQUEST_PARAM.getClassName())
                .addMember("value", "$S", paramName).build();
    }

    @Override
    public Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId) {
        List<AnnotationSpec> annotations = new ArrayList<>();

        String httpMethod = getHttpMethod();

        AnnotationSpec.Builder builder = AnnotationSpec.builder(SPRING_REQUEST_MAPPING.getClassName());

        builder.addMember("method", "$T." + httpMethod.toUpperCase(), SPRING_REQUEST_METHOD.getClassName());
        if (applyId) {
            builder.addMember("path", "$S", "/{id}");
        }
        //builder.addMember("consumes", "$T.APPLICATION_JSON", SPRING_HTTP_MEDIA_TYPE.getClassName());
        builder.addMember("produces", "$T.APPLICATION_JSON_UTF8_VALUE", SPRING_HTTP_MEDIA_TYPE.getClassName());

        annotations.add(builder.build());
        annotations.add(createAnnotation(SPRING_RESPONSE_BODY));

        return annotations;
    }

    @Override
    public AnnotationTypes getPathVariableAnnotationType() {
        return SPRING_REQUEST_PARAM;
    }

    @Override
    public void generateMethodNotAllowedStatement(MethodSpec.Builder builder) {
        //new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED)
        builder.addStatement("return new $T<>($T.METHOD_NOT_ALLOWED)", getMethodNowAllowedReturnType(), SPRING_HTTP_STATUS.getClassName());
    }

    @Override
    public ClassName getMethodNowAllowedReturnType() {
        return SPRING_RESPONSE_ENTITY.getClassName();
    }
}
