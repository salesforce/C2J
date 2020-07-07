using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using Newtonsoft.Json;
using TestAssembly;

namespace SFLibReflector
{
    internal abstract class Reflector
    {
        internal static IEnumerable<Type> GetAllClasses(string assemblyName)
        {
            var a = Assembly.Load(assemblyName);
            a.GetName();

            var types = a.GetTypes()
                .Where(x => !x.IsInterface /*&& !x.IsAbstract*/ && x.IsClass)
                .Where(c => c.Name != "<>c" &&
                            !c.Name.StartsWith("<>c__DisplayClass") && !c.Name.StartsWith("<>f__AnonymousType"))
                .Where(c =>
                    //!new[] {"Object", "DefaultContractResolver"}.Contains(c.BaseType.Name)
                    c.IsDefined(typeof(JsonObjectAttribute), true))
                .Where(c => !c.IsGenericType)
                .Where(c => !c.IsSubclassOf(typeof(Exception)))
                .Where(c => !c.IsSubclassOf(typeof(Attribute)));

            return types.ToArray();
        }

        private static bool IsSerializable(PropertyInfo p)
        {
            //include:  public, jsonProperty
            //ignore: JsonIgnore, Private
            //    [JsonProperty("Duration")] => field named Duration

            if (p.IsDefined(typeof(JsonIgnoreAttribute), true))
                return false;
            if (p.IsDefined(typeof(JsonPropertyAttribute), true))
                return true;
            return p.CanWrite && p.GetSetMethod(true).IsPublic;
        }

        private static string PropertyName(PropertyInfo p)
        {
            if (!p.IsDefined(typeof(JsonPropertyAttribute), true))
                return p.Name;
            var attr =
                p.GetCustomAttributes(true).First(a => a.GetType() == typeof(JsonPropertyAttribute)) as
                    JsonPropertyAttribute;

            return attr.PropertyName ?? p.Name;
        }

        private static string GetPropertyType(PropertyInfo pi)
        {
            // List<T>
            if (pi.PropertyType.IsGenericType && pi.PropertyType.GetGenericTypeDefinition() == typeof(List<>))
                return $"List<{pi.PropertyType.GetGenericArguments()[0].Name}>";

            // RelatedObjectCollection<T>
            var type = pi.PropertyType.Name;
            if (type == "RelatedObjectCollection`1")
                return $"RelatedObjectCollection<{pi.PropertyType.GetGenericArguments()[0].Name}>";

            var isNullable = false;
            //Nullable
            if (pi.PropertyType.IsGenericType && pi.PropertyType.GetGenericTypeDefinition() == typeof(Nullable<>))
            {
                type = pi.PropertyType.GetGenericArguments()[0].Name;
                isNullable = true;
            }

            // nullable types: Integer, Boolean, Double
            // non-nullable types:  int, boolean, double

            switch (type)
            {
                case "Int32" when isNullable:
                case "Int64" when isNullable:
                    return "Integer";
                case "Int32":
                case "Int64":
                    return "int";

                case "Double" when isNullable:
                    return "Double";
                case "Double":
                    return "double";
                case "DateTime":
                    return "OffsetDateTime";
                case "TimeZoneInfo":
                    return "TimeZone";

                case "Boolean" when !isNullable:
                    return "boolean";

                case "Boolean":
                case "String":
                case "SFAttributes":
                case "DayOfWeek":
                case "ServiceAppointmentInnerDetails":
                    return type;

                default:
                    return type;
            }
        }

        private static string ToYaml(Type t)
        {
            var properties = t.GetProperties(BindingFlags.Public | BindingFlags.Instance | BindingFlags.NonPublic |
                                             BindingFlags.DeclaredOnly);
            properties = properties.Where(IsSerializable).ToArray();

            var yml = new StringBuilder($"\n{t.Name}: {t.BaseType.Name}\n");

            // if(!properties.Any())
            //return $"# {t.Name}";

            foreach (var m in properties)
                yml.AppendLine(
                    $"\t{PropertyName(m)}: {GetPropertyType(m)} {(PropertyName(m) != m.Name ? m.Name : "")}");

            return yml.ToString();
        }

        public static string Reflect()
        {
            var assemblyName = "TestAssembly";
            var t = typeof(Employee);
            var a = Assembly.Load(assemblyName);
            var classes =
                GetAllClasses(assemblyName) //.Where(t => t.IsDefined(typeof(JsonObjectAttribute), true))
                    .OrderBy(c => c.Name).ToList();

            var reflect = new StringBuilder($"# {classes.Count()} Classes");
            classes = classes;
            foreach (var c in classes)
                reflect.AppendLine(ToYaml(c));

            return reflect.ToString();
        }
    }
}