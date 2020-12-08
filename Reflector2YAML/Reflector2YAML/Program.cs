/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

using System;
using System.IO;
using System.Net;

namespace SFLibReflector
{
    class Program
    {
        static void Main(string[] args)
        {
            var reflected = Reflector.Reflect();
            Console.WriteLine(reflected);

            var folder =new DirectoryInfo(Directory.GetCurrentDirectory()).Parent.Parent.Parent.Parent.Parent;
            var yamlFilename = Path.Combine(folder.FullName, "YAMLParser","dataModel.yaml");
            Console.WriteLine(yamlFilename);
            File.WriteAllText(yamlFilename,reflected);
        }
    }
}