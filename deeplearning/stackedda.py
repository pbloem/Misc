import os, random, time, math

import theano as th
import theano.tensor as T
from theano.tensor.shared_randomstreams import RandomStreams

from PIL import Image, ImageOps
import IPython.display as disp
from io import BytesIO
import numpy as np
import matplotlib.pyplot as plt
from matplotlib import colors
from ipywidgets import widgets 

import da

# First we define a class representing a basic hidden layer
class HiddenLayer(object):
    def __init__(self, rng, input, n_in, n_out, W=None, b=None,
                 activation=T.tanh):
        self.input = input

        if W is None:
            W_values = np.asarray(
                rng.uniform(
                    low=-np.sqrt(6. / (n_in + n_out)),
                    high=np.sqrt(6. / (n_in + n_out)),
                    size=(n_in, n_out)
                ),
                dtype=th.config.floatX
            )
            if activation == th.tensor.nnet.sigmoid:
                W_values *= 4

            W = th.shared(value=W_values, name='W', borrow=True)

        if b is None:
            b_values = np.zeros((n_out,), dtype=th.config.floatX)
            b = th.shared(value=b_values, name='b', borrow=True)

        self.W = W
        self.b = b

        lin_output = T.dot(input, self.W) + self.b
        self.output = (
            lin_output if activation is None
            else activation(lin_output)
        )
        # parameters of the model
        self.params = [self.W, self.b]



# Now the class representing the stacked auto-encoder 
class SdA(object):
    def __init__(
        self,
        numpy_rng,
        input=input,
        theano_rng=None,
        n_ins=784,
        hidden_layers_sizes=[20000]
    ):
        self.sigmoid_layers = []
        self.dA_layers = []
        self.params = []
        self.n_layers = len(hidden_layers_sizes)

        assert self.n_layers > 0

        if not theano_rng:
            theano_rng = RandomStreams(numpy_rng.randint(2 ** 30))
            
        # allocate symbolic variables for the data
        self.x = input # the data is presented as rasterized images

        for i in range(self.n_layers):
                # construct the sigmoidal layer

                # the size of the input is either the number of hidden units of
                # the layer below or the input size if we are on the first layer
                if i == 0:
                    input_size = n_ins
                else:
                    input_size = hidden_layers_sizes[i - 1]

                # the input to this layer is either the activation of the hidden
                # layer below or the input of the SdA if you are on the first
                # layer
                if i == 0:
                    layer_input = self.x
                else:
                    layer_input = self.sigmoid_layers[-1].output

                sigmoid_layer = HiddenLayer(rng=numpy_rng,
                                            input=layer_input,
                                            n_in=input_size,
                                            n_out=hidden_layers_sizes[i],
                                            activation=T.nnet.sigmoid)
                # add the layer to our list of layers
                self.sigmoid_layers.append(sigmoid_layer)
                # its arguably a philosophical question...
                # but we are going to only declare that the parameters of the
                # sigmoid_layers are parameters of the StackedDAA
                # the visible biases in the dA are parameters of those
                # dA, but not the SdA
                self.params.extend(sigmoid_layer.params)

                # We re-use our da class, and give it the shared weight
                # matrix from this stacked autoencoder
                dA_layer = da.dA(numpy_rng=numpy_rng,
                              theano_rng=theano_rng,
                              input=layer_input,
                              n_visible=input_size,
                              n_hidden=hidden_layers_sizes[i],
                              W=sigmoid_layer.W,
                              bhid=sigmoid_layer.b)
                self.dA_layers.append(dA_layer)
                
    def pretraining_functions(self):
        ''' Returns training functions for each layer
        '''

        print('pretraining in')
        # index to a [mini]batch
        corruption_level = T.scalar('corruption')  # % of corruption to use
        learning_rate = T.scalar('lr')  # learning rate to use
        
        pretrain_fns = []
        
        for dA in self.dA_layers:

            # get the cost and the updates list
            cost, updates = dA.get_cost_updates(corruption_level,
                                                learning_rate)
            # compile the theano function
            fn = th.function(
                inputs=[
                    self.x,
                    th.In(corruption_level, value=0.01),
                    th.In(learning_rate, value=0.1)
                ],
                outputs=cost,
                updates=updates
            )
            
            # append `fn` to the list of functions
            pretrain_fns.append(fn)

            
        print('pretraining out')

        return pretrain_fns 
    
    def get_hidden_values(self, input):
        result = input
        for layer in self.dA_layers:
            result = layer.get_hidden_values(result)
        return result

    def get_reconstructed_input(self, hidden):
        result = hidden
        for layer in reversed(self.dA_layers):
            result = layer.get_reconstructed_input(result)
        return result
    