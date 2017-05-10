package it.sorintlab.lambda;

import static com.codepoetics.protonpack.StreamUtils.zipWithIndex;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.mdkt.compiler.InMemoryJavaCompiler;

public class Main {

	public static final class ParameterizedStreamType implements ParameterizedType {

		private final Type actualTypeArgument;

		public ParameterizedStreamType(Type actualTypeArgument) {
			this.actualTypeArgument = actualTypeArgument;
		}

		@Override
		public Type getRawType() {
			return Stream.class;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return new Type[] { actualTypeArgument };
		}

		@Override
		public String toString() {
			return String.format("java.util.stream.Stream<%s>", actualTypeArgument.getTypeName());
		}
	}

	private static final List<Method> STREAM_METHODS = unmodifiableList(Stream.of(Stream.class.getDeclaredMethods()) //
			.filter(m -> !m.getName().equals("collect")) //
			.filter(m -> !Modifier.isStatic(m.getModifiers())) //
			.filter(m -> m.getReturnType().equals(Stream.class)) //
			.collect(toList()));

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		List<Option> options = new ArrayList<>();
		try {
			for (int i = 0; i < args.length; i++) {
				final String optionName = args[i];
				if (optionName.charAt(0) != '-') {
					throw new UnrecognizedOptionException(optionName);
				}

				final String methodName = optionName.substring(1);

				final List<Method> methods = STREAM_METHODS.stream() //
						.filter(m -> m.getName().equals(methodName)) //
						.collect(toList());
				if (methods.isEmpty()) {
					throw new UnrecognizedOptionException(optionName);
				}

				final List<String> foundParams = new ArrayList<>();
				for (int j = i + 1; j < args.length; j++) {
					final String param = args[j];
					if (param.charAt(0) == '-' && !param.matches("-\\d+")) {
						break;
					}

					foundParams.add(args[j]);
					i++;
				}
				final List<Method> validMethods = methods.stream() //
						.filter(method -> method.getParameterTypes().length == foundParams.size()) //
						.collect(toList());
				if (validMethods.size() != 1) {
					throw new InvalidOptionParamsException(optionName, foundParams);
				}

				final Method method = validMethods.get(0);
				options.add(new Option(method, foundParams));
			}
		} catch (UnrecognizedOptionException e) {
			System.err.println("Unrecognized option : " + e.getMessage());
			System.exit(1);
		} catch (InvalidOptionParamsException e) {
			System.err.println("Invalid arguments for option : " + e.getMessage());
			System.exit(1);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}

		final StringBuilder source = new StringBuilder() //
				.append("package it.sorintlab.lambda.synth;\n") //
				.append("import static java.util.Arrays.asList;\n") //
				.append("import java.util.*;\n") //
				.append("import java.util.stream.*;\n") //
				.append("public class ParamsLookup extends java.util.HashMap<List<Integer>, Object> {{\n") //
				.append(zipWithIndex(options.stream()) //
						.flatMap(iopt -> {
							final Option option = iopt.getValue();
							final long optionIndex = iopt.getIndex();

							return zipWithIndex(option.getParams().stream()) //
									.map(iparam -> {
										final long paramIndex = iparam.getIndex();
										final Param param = iparam.getValue();
										return String.format("\t\tthis.put(asList(%d, %d), (%s) %s);", optionIndex,
												paramIndex, param.getType(), param.getSource());
									});
						}) //
						.collect(joining("\n"))) //
				.append("\n}}");

		Map<Integer, Object> paramsLookup;
		try {
			Class<?> paramsLookupClass = InMemoryJavaCompiler.compile("it.sorintlab.lambda.synth.ParamsLookup",
					source.toString());
			paramsLookup = (Map<Integer, Object>) paramsLookupClass.newInstance();
		} catch (Exception e) {
			System.err.println("Error synthesizing method params : " + e.getMessage());
			System.exit(1);
			return;
		}

		//options.forEach(System.out::println);
		
		try (InputStreamReader reader = new InputStreamReader(System.in);
				BufferedReader in = new BufferedReader(reader)) {

			Stream<?> stream = in.lines();

			int optionIndex = 0;
			for (Option option : options) {

				final Method method = option.getMethod();
				final int paramsNumber = option.getParams().size();

				final Object[] params = new Object[paramsNumber];
				for (int paramIndex = 0; paramIndex < paramsNumber; paramIndex++) {
					params[paramIndex] = paramsLookup.get(asList(optionIndex, paramIndex));
				}
				optionIndex++;

				stream = (Stream<?>) method.invoke(stream, params);
			}

			stream.forEach(System.out::println);

		} catch (IOException e) {
			System.err.println("IO Error : " + e.getMessage());
			System.exit(1);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.err.println("Pipeline invocation error : " + e.getMessage());
			e.printStackTrace();
		}
	}

}
