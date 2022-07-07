package io.smallrye.mutiny.vertx.codegen.methods;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.codegen.lang.TypeHelper;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.codegen.type.TypeReflectionFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ConsumerMethodGenerator extends MutinyMethodGenerator {

    public ConsumerMethodGenerator(PrintWriter writer) {
        super(writer);
    }

    public void generateDeclaration(MethodInfo method) {
        MutinyMethodDescriptor consumerMethod = computeMethodInfo(method);
        generateJavadoc(consumerMethod);
        generateMethodDeclaration(consumerMethod);
        writer.println(";");
        writer.println();
    }

    public void generate(MethodInfo method) {
        MutinyMethodDescriptor consumerMethod = computeMethodInfo(method);
        generateJavadoc(consumerMethod);
        generateMethodDeclaration(consumerMethod);
        generateBody(consumerMethod);
        writer.println();
    }

    private void generateBody(MutinyMethodDescriptor descriptor) {
        MethodInfo method = descriptor.getMethod();
        writer.println(" {");
        writer.print("    ");
        if (!method.getReturnType().isVoid()) {
            writer.print("return ");
        }

        // TODO Inline method body here.
        writer.print("__" + method.getName() + "(");
        List<ParamInfo> params = method.getParams();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                writer.print(", ");
            }
            ParamInfo param = params.get(i);
            if (i < params.size() - 1) {
                writer.print(param.getName());
            } else {
                if (param.getType().getName().startsWith(Uni.class.getName())) {
                    writer.print(param.getName());
                } else if(param.getType().getName().equals(Runnable.class.getName())) {
                    writer.println("ignored -> " + param.getName() + ".run()");
                } else {
                    writer.print(param.getName() + " != null ? new io.smallrye.mutiny.vertx.DelegatingConsumerHandler(" + param.getName() + ") : null");
                }
            }
        }
        writer.println(");");
        writer.println("  }");
    }

    private MutinyMethodDescriptor computeMethodInfo(MethodInfo method) {
        List<ParamInfo> params = new ArrayList<>(method.getParams());
        // Remove the last Handler<T> parameter
        ParamInfo handlerParameter = params.remove(method.getParams().size() - 1);
        // Extract T:
        TypeInfo consumerType = ((ParameterizedTypeInfo) handlerParameter.getType()).getArg(0);
        TypeInfo consumerUnresolvedType = ((ParameterizedTypeInfo) handlerParameter.getUnresolvedType()).getArg(0);

        TypeInfo consumer = new io.vertx.codegen.type.ParameterizedTypeInfo(
                io.vertx.codegen.type.TypeReflectionFactory.create(Consumer.class).getRaw(),
                consumerUnresolvedType.isNullable(), Collections.singletonList(consumerType));

        if (TypeHelper.isConsumerOfPromise(consumer)) {
            TypeInfo inner = ((ParameterizedTypeInfo) consumerType).getArg(0);
            consumer = new io.vertx.codegen.type.ParameterizedTypeInfo(
                    io.vertx.codegen.type.TypeReflectionFactory.create(Uni.class).getRaw(),
                    false, Collections.singletonList(inner));
        } else if (TypeHelper.isConsumerOfVoid(consumer)) {
            consumer = TypeReflectionFactory.create(Runnable.class);
        }

        // Replace the removed Handler<T> by the computed Consumer<T>
        params.add(
                new ParamInfo(params.size(), handlerParameter.getName(), handlerParameter.getDescription(), consumer));

        return new MutinyMethodDescriptor(method.copy().setParams(params), method,
                MutinyMethodDescriptor.MutinyKind.CONSUMER);
    }

}
