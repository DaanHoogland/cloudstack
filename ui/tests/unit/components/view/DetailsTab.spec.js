// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import { flushPromises } from '@vue/test-utils'

import common from '../../../common'
import DetailsTab from '@/components/view/DetailsTab'

const state = {
  user: {
    info: {
      roletype: 'Admin'
    }
  }
}

const messages = {
  en: {
    'label.user.data': 'User Data',
    'label.cniconfiguration': 'CNI Configuration'
  }
}

const createWrapper = async (routeName) => {
  const router = common.createMockRouter([
    {
      path: '/:id',
      name: routeName,
      component: { template: '<div />' },
      meta: { name: routeName, details: ['userdata'] }
    }
  ])
  const store = common.createMockStore(state)
  const i18n = common.createMockI18n('en', messages)

  await router.push('/test-id')
  await router.isReady()

  return common.createFactory(DetailsTab, {
    router,
    store,
    i18n,
    props: {
      resource: {
        userdata: 'dGVzdA=='
      }
    }
  })
}

describe('Components > View > DetailsTab.vue', () => {
  it('shows user data label in userdata details route', async () => {
    const wrapper = await createWrapper('userdata')
    await flushPromises()

    expect(wrapper.html()).toContain('<strong>User Data</strong>')
  })

  it('shows cni configuration label in cni configuration details route', async () => {
    const wrapper = await createWrapper('cniconfiguration')
    await flushPromises()

    expect(wrapper.html()).toContain('<strong>CNI Configuration</strong>')
  })
})
