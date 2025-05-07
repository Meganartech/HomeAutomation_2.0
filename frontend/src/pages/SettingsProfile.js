import SidePanel from '../components/SidePanel';
import Navbar from '../components/Navbar';
import Indicator from '../components/Indicator';
import InsideSidePanel from '../components/InsideSidePanel';
import ProfileContent from '../components/ProfileContent';

export default function SettingsProfile() {

    return (
        <>
            {/* Background */}
            <div className="bg-eaeaea container-fluid position-fixed px-0">

                {/* Row */}
                <div className="d-flex">

                    {/* Column-1 */}
                    <div style={{ width: '300px' }}>
                        <SidePanel />
                    </div>

                    {/* Column-2 */}
                    <div style={{ width: 'calc(100% - 300px)', height: '100vh', overflowY: 'hidden' }}>

                        {/* Row-1 */}
                        <Navbar />

                        {/* Row-2 */}
                        <Indicator />

                        {/* Row-3 */}
                        <div style={{ height: 'calc(100vh - 180px)', boxShadow: '0px 0px 24.7px 0px #00000026', borderRadius: '8px', overflowY: 'hidden' }} className="bg-ffffff m-3">

                            {/* Row */}
                            <div className='d-flex'>

                                {/* Column-1 */}
                                <div style={{ width: '240px' }}>
                                    <InsideSidePanel />
                                </div>

                                {/* Column-2 */}
                                <div style={{ width: 'calc(100% - 240px)', height: '100vh', overflowY: 'hidden' }}>

                                    {/* Row */}
                                    <ProfileContent />

                                </div>

                            </div>

                        </div>

                    </div>

                </div >

            </div >

        </>
    );
};