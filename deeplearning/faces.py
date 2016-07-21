import os, random, time, math, glob
import theano as th
import theano.tensor as T
from PIL import Image, ImageOps
import IPython.display as disp
from io import BytesIO
import numpy as np
import matplotlib.pyplot as plt
from matplotlib import colors
from ipywidgets import widgets 

import stackedda

path_plates = '~/Documents/Datasets/tabular/plates'
path_faces  = '~/Documents/Datasets/tabular/humanea'

output = './output'

os.makedirs(output, exist_ok=True)

# We'll load only the first 2000 of each
num_instances = 2000 
num_features = 60 * 60 * 3 # each channel of each pixel is a feature

# Load the images into a big matrix
plates = np.zeros((num_instances, num_features)) # matrix of zeros
faces = np.zeros((num_instances, num_features))
    
i = 0
for f in glob.glob(path_faces + '/*.png')[0:num_instances]:
    vector = np.ravel(np.asarray(Image.open(f))) / 255.0 # Load the image as a flat vector
    faces[i, :] = vector # set this image as the i'th row of the matrix
    i += 1
    
data = faces

print('Data read.')

iterations = 10000
batch_size = 100
learning_rate = 0.0001

numpy_rng = np.random.RandomState(89677)

x_sda = T.matrix('x_sda')

# construct the stacked denoising autoencoder class
sda = stackedda.SdA(
    input=x_sda,
    numpy_rng=numpy_rng,
    n_ins= 60 * 60 * 3,
    hidden_layers_sizes=[10000, 1000, 500, 100, 200, 100]
)
print('SDA constru cted.')

pretraining_fns = sda.pretraining_functions()

print('Training the DA')
for l in range(sda.n_layers):
    print('training layer {}'.format(l))
    for i in range(iterations):
        # select a mini-batch
        ix = np.random.randint(data.shape[0], size=batch_size)
        batch = data[ix, :]
    
        # perform the update
        error = pretraining_fns[l](batch)
    
        if i % 100 == 0:
            print('\titeration {}) error = {}'.format(i, error))

# Function for running the trained network
hiddens = sda.get_hidden_values(x_sda)
reduce = th.function([x_sda], hiddens)

visibs = sda.get_reconstructed_input(hiddens)
expand = th.function([hiddens], visibs)

# Sample two random images
im1 = data[random.randint(0, data.shape[0]-1), :] 
im2 = data[random.randint(0, data.shape[0]-1), :] 

# reduce 

h1 = reduce(im1.reshape((1, im1.shape[0])))
h2 = reduce(im2.reshape((1, im2.shape[0])))

print('first image')
vector = im1.reshape(old_shape) * 255.0 # flatten
vector = vector.astype('uint8')    # convert to integers

image = Image.fromarray(vector)    # convert to image

image.save(output + '/from.png', format='png')

print('second image')
vector = im2.reshape(old_shape) * 255.0 # flatten
vector = vector.astype('uint8')    # convert to integers

image = Image.fromarray(vector)    # convert to image

image.save(output + '/', format='png')

inbetweens = 10

for a in np.linspace(0.0, 1.0, 5):
    print (a)
    
    h_mid = h2 * a + h1 * (1.0 - a)
    im_mid = expand(h_mid)
        
    vector = im_mid.reshape(old_shape) * 255.0 # flatten
    vector = vector.astype('uint8')    # convert to integers

    image = Image.fromarray(vector)    # convert to image
    image.save('{0}/{0}.png'.format(output, a), format='png')
