/** -*- mode: C++;-*-
 * @file Model.h
 * @author Bruno Goncalves
 *
 * Created by Bruno Goncalves on 09/14/08
 * Copywright 2008 Bruno Goncalves. All rights reserved.
 *
 */

#ifndef _MODEL_H_
#define _MODEL_H_

#include <defs.h>
#include <Parser.h>
#include <vector>
#include <map>
#include <Rand.h>
#include <stdint.h>
#include <sstream>

/**
 * @class Model
 *
 */
class Model
{
  std::map<std::string, unsigned> states;
  std::vector<std::vector<Transition> > transitions;
  std::vector<unsigned> newState;
  std::vector<std::string> files;
  std::vector<std::vector<std::vector<double> > > rates; 
  Rand *r;
  std::vector<std::string> labels;
  double *p;
  unsigned *n;

 public:
  Model(Parser &parse, Rand &r2);

  std::vector<std::vector<std::vector<double> > > getRates()
  {
    return rates;
  }

  /**
   * @param current
   * @param N
   * @param x
   * @param y
   * @param fp
   * @return The number of secondary infections
   */
  int step(std::vector<int64_t> &current, int64_t N, int x, int y, FILE **fp);
};

#endif /* _MODEL_H_ */ 
